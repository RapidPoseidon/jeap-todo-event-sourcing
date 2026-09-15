package ch.example.todo.task.domain.event;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultEventTypeMapper implements EventTypeMapper {

    @Override
    public Class<? extends Event> getClassByEventType(String eventType) {
        return EventType.valueOf(eventType).getEventClass();
    }
}
