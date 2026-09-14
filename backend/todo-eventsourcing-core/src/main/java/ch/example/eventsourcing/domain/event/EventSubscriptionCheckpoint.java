package ch.example.eventsourcing.domain.event;

import java.math.BigInteger;

/**
 * Position of an asynchronous subscription in the global event stream.
 */
public record EventSubscriptionCheckpoint(BigInteger lastProcessedTransactionId, long lastProcessedEventId) {
}
