package ch.example.eventsourcing.service;

import ch.example.eventsourcing.service.event.AsyncEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ScheduledEventSubscriptionProcessor {

    private final List<AsyncEventHandler> eventHandlers;
    private final EventSubscriptionProcessor eventSubscriptionProcessor;

    @Scheduled(
            fixedDelayString = "${event-sourcing.polling-subscriptions.polling-interval:PT1S}",
            initialDelayString = "${event-sourcing.polling-subscriptions.polling-initial-delay:PT1S}")
    public void processNewEvents() {
        eventHandlers.forEach(this::processNewEvents);
    }

    private void processNewEvents(AsyncEventHandler eventHandler) {
        try {
            eventSubscriptionProcessor.processNewEvents(eventHandler);
        } catch (Exception e) {
            log.warn("Failed to handle new events for subscription %s".formatted(eventHandler.getSubscriptionName()), e);
        }
    }
}
