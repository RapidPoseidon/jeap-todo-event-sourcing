package ch.example.eventsourcing.domain.event;

import java.math.BigInteger;

/**
 * A persisted event together with the global sequence id and the transaction id that ordered it.
 */
public record EventWithId<T extends Event>(long id, BigInteger transactionId, T event) {
}
