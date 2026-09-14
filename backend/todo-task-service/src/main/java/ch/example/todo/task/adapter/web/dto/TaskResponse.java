package ch.example.todo.task.adapter.web.dto;

import ch.example.todo.task.adapter.persistence.TaskProjection;
import ch.example.todo.task.domain.TaskAggregate;
import ch.example.todo.task.domain.TaskPriority;
import ch.example.todo.task.domain.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        int version,
        TaskStatus status,
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate,
        String owner,
        String assignee,
        String createdBy,
        OffsetDateTime createdDate,
        OffsetDateTime lastModifiedDate,
        OffsetDateTime completedDate,
        int completionCount) {

    public static TaskResponse of(TaskProjection task) {
        return new TaskResponse(task.id(), task.version(), task.status(), task.title(), task.description(),
                task.priority(), task.dueDate(), task.owner(), task.assignee(), task.createdBy(),
                task.createdDate(), task.lastModifiedDate(), task.completedDate(), task.completionCount());
    }

    public static TaskResponse of(TaskAggregate task) {
        return new TaskResponse(task.getAggregateId(), task.getVersion(), task.getStatus(), task.getTitle(),
                task.getDescription(), task.getPriority(), task.getDueDate(), task.getOwner(), task.getAssignee(),
                task.getCreatedBy(), task.getCreatedDate(), task.getLastModifiedDate(), task.getCompletedDate(),
                task.getCompletionCount());
    }
}
