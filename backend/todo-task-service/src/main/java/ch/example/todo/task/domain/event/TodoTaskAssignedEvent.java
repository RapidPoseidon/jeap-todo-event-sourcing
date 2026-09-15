package ch.example.todo.task.domain.event;

import ch.example.eventsourcing.domain.event.Event;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public final class TodoTaskAssignedEvent extends Event {

    private final String assignee;

    @JsonCreator
    @Builder
    public TodoTaskAssignedEvent(UUID aggregateId,
                                 int version,
                                 OffsetDateTime createdDate,
                                 String initiatedBy,
                                 String assignee) {
        super(aggregateId, version, createdDate, initiatedBy);
        this.assignee = assignee;
    }

    @Override
    public String getEventType() {
        return EventType.TODO_TASK_ASSIGNED.name();
    }
}
