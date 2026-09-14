package ch.example.eventsourcing.domain.event;

/**
 * Maps the event type persisted in the event store to the event class to deserialize into.
 */
public interface EventTypeMapper {

    Class<? extends Event> getClassByEventType(String eventType);
}
