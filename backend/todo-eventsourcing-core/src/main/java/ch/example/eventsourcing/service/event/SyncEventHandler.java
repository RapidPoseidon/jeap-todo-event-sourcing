package ch.example.eventsourcing.service.event;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;

import java.util.List;

/**
 * Handles new events in the same transaction that appended them; used for read models that must never lag.
 */
public interface SyncEventHandler {

    void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate);

    String getAggregateType();
}
