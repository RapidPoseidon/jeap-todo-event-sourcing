package ch.example.todo.task.domain.event;

import ch.example.eventsourcing.domain.event.Event;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@ToString(callSuper = true)
public final class TodoTaskCompletedEvent extends Event {

    @JsonCreator
    @Builder
    public TodoTaskCompletedEvent(UUID aggregateId,
                             int version,
                             OffsetDateTime createdDate,
                             String initiatedBy) {
        super(aggregateId, version, createdDate, initiatedBy);
    }

    @Override
    public String getEventType() {
        return EventType.TODO_TASK_COMPLETED.name();
    }
}
