package ch.example.eventsourcing.service;

import ch.example.eventsourcing.service.event.AsyncEventHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGNotification;
import org.postgresql.jdbc.PgConnection;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Wakes subscriptions on the PostgreSQL {@code channel_event_notify} channel instead of polling.
 * The dedicated connection is outside the pool because {@code getNotifications} blocks it indefinitely.
 * Listening starts on context refresh, once the schema migration that creates the tables has run.
 */
@RequiredArgsConstructor
@Slf4j
public class PostgresChannelEventSubscriptionProcessor {

    private static final String CHANNEL = "channel_event_notify";

    private final List<AsyncEventHandler> eventHandlers;
    private final EventSubscriptionProcessor eventSubscriptionProcessor;
    private final DataSourceProperties dataSourceProperties;
    private final ExecutorService executor = newExecutor();

    private CountDownLatch latch = new CountDownLatch(0);
    private Future<?> future = CompletableFuture.completedFuture(null);
    private volatile PgConnection connection;

    private static ExecutorService newExecutor() {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("postgres-channel-event-subscription-");
        threadFactory.setDaemon(true);
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    @EventListener(ContextRefreshedEvent.class)
    public synchronized void start() {
        if (latch.getCount() > 0) {
            return;
        }
        latch = new CountDownLatch(1);
        future = executor.submit(this::listen);
    }

    @PreDestroy
    public synchronized void stop() {
        if (future.isDone()) {
            return;
        }
        future.cancel(true);
        PgConnection conn = connection;
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // Closing the connection is how the blocking notification poll is interrupted.
            }
        }
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                log.warn("Failed to stop the PostgreSQL notification listener within 5 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void listen() {
        try {
            while (isActive()) {
                try (PgConnection conn = openConnection()) {
                    try (Statement statement = conn.createStatement()) {
                        statement.execute("LISTEN " + CHANNEL);
                    }
                    connection = conn;
                    eventHandlers.forEach(this::processNewEvents);
                    pollNotifications(conn);
                } catch (Exception e) {
                    if (isActive()) {
                        log.error("Failed to poll notifications from the PostgreSQL database", e);
                    }
                }
            }
        } finally {
            latch.countDown();
        }
    }

    private void pollNotifications(PgConnection conn) throws SQLException {
        while (isActive()) {
            PGNotification[] notifications = conn.getNotifications(0);
            if (!isActive()) {
                return;
            }
            if (notifications == null) {
                continue;
            }
            for (PGNotification notification : notifications) {
                eventHandlers.stream()
                        .filter(eventHandler -> eventHandler.getAggregateType().equals(notification.getParameter()))
                        .forEach(this::processNewEvents);
            }
        }
    }

    private PgConnection openConnection() throws SQLException {
        return DriverManager.getConnection(
                        dataSourceProperties.determineUrl(),
                        dataSourceProperties.determineUsername(),
                        dataSourceProperties.determinePassword())
                .unwrap(PgConnection.class);
    }

    private boolean isActive() {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private void processNewEvents(AsyncEventHandler eventHandler) {
        try {
            eventSubscriptionProcessor.processNewEvents(eventHandler);
        } catch (Exception e) {
            log.warn("Failed to handle new events for subscription %s".formatted(eventHandler.getSubscriptionName()), e);
        }
    }
}
