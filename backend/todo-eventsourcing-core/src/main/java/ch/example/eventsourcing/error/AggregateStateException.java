package ch.example.eventsourcing.error;

import lombok.NonNull;

/**
 * A command was rejected because the aggregate is not in a state that allows it.
 */
public class AggregateStateException extends RuntimeException {

    public AggregateStateException(@NonNull String message, Object... args) {
        super(message.formatted(args));
    }
}
