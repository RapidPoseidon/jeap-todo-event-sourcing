package ch.example.eventsourcing.error;

/**
 * Another transaction appended events to the aggregate while this command was being processed.
 */
public class OptimisticConcurrencyControlException extends AggregateStateException {

    public OptimisticConcurrencyControlException(int expectedVersion) {
        super("Actual version doesn't match expected version %s", expectedVersion);
    }
}
