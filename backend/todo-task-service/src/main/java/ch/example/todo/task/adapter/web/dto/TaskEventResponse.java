package ch.example.todo.task.adapter.web.dto;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

public record TaskEventResponse(
        long id,
        int version,
        String eventType,
        OffsetDateTime createdDate,
        String initiatedBy,
        JsonNode payload) {

    public static TaskEventResponse of(EventWithId<Event> eventWithId, ObjectMapper objectMapper) {
        Event event = eventWithId.event();
        return new TaskEventResponse(
                eventWithId.id(),
                event.getVersion(),
                event.getEventType(),
                event.getCreatedDate(),
                event.getInitiatedBy(),
                objectMapper.valueToTree(event));
    }
}
