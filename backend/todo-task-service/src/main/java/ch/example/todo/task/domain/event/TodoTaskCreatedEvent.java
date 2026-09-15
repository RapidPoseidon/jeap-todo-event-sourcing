package ch.example.todo.task.domain.event;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.todo.task.domain.TaskPriority;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public final class TodoTaskCreatedEvent extends Event {

    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final LocalDate dueDate;
    private final String owner;

    @JsonCreator
    @Builder
    public TodoTaskCreatedEvent(UUID aggregateId,
                                int version,
                                OffsetDateTime createdDate,
                                String initiatedBy,
                                String title,
                                String description,
                                TaskPriority priority,
                                LocalDate dueDate,
                                String owner) {
        super(aggregateId, version, createdDate, initiatedBy);
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.owner = owner;
    }

    @Override
    public String getEventType() {
        return EventType.TODO_TASK_CREATED.name();
    }
}
