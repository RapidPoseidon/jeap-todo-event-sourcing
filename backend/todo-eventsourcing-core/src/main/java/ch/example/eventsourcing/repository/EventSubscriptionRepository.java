package ch.example.eventsourcing.repository;

import ch.example.eventsourcing.domain.event.EventSubscriptionCheckpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class EventSubscriptionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void createSubscriptionIfAbsent(String subscriptionName) {
        jdbcTemplate.update("""
                        INSERT INTO ES_EVENT_SUBSCRIPTION (SUBSCRIPTION_NAME, LAST_TRANSACTION_ID, LAST_EVENT_ID)
                        VALUES (:subscriptionName, '0'::xid8, 0)
                        ON CONFLICT DO NOTHING
                        """,
                Map.of("subscriptionName", subscriptionName));
    }

    /**
     * Takes the subscription's row lock, or returns empty if another instance already holds it.
     */
    public Optional<EventSubscriptionCheckpoint> readCheckpointAndLockSubscription(String subscriptionName) {
        return jdbcTemplate.query("""
                        SELECT LAST_TRANSACTION_ID::text,
                               LAST_EVENT_ID
                          FROM ES_EVENT_SUBSCRIPTION
                         WHERE SUBSCRIPTION_NAME = :subscriptionName
                           FOR UPDATE SKIP LOCKED
                        """,
                Map.of("subscriptionName", subscriptionName),
                this::toCheckpoint).stream().findFirst();
    }

    public boolean updateEventSubscription(String subscriptionName,
                                           BigInteger lastProcessedTransactionId,
                                           long lastProcessedEventId) {
        int updatedRows = jdbcTemplate.update("""
                        UPDATE ES_EVENT_SUBSCRIPTION
                           SET LAST_TRANSACTION_ID = :lastProcessedTransactionId::xid8,
                               LAST_EVENT_ID = :lastProcessedEventId
                         WHERE SUBSCRIPTION_NAME = :subscriptionName
                        """,
                Map.of("subscriptionName", subscriptionName,
                        "lastProcessedTransactionId", lastProcessedTransactionId.toString(),
                        "lastProcessedEventId", lastProcessedEventId));
        return updatedRows > 0;
    }

    private EventSubscriptionCheckpoint toCheckpoint(ResultSet rs, int rowNum) throws SQLException {
        return new EventSubscriptionCheckpoint(
                new BigInteger(rs.getString("LAST_TRANSACTION_ID")),
                rs.getLong("LAST_EVENT_ID"));
    }
}
