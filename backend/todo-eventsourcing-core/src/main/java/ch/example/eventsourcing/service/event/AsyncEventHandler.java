package ch.example.eventsourcing.service.event;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;

/**
 * Handles events after commit, at-least-once, from a checkpointed subscription.
 */
public interface AsyncEventHandler {

    void handleEvent(EventWithId<Event> event);

    String getAggregateType();

    default String getSubscriptionName() {
        return getClass().getName();
    }
}
