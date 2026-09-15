package ch.example.todo.task.adapter.persistence;

import ch.example.todo.task.domain.TaskPriority;
import ch.example.todo.task.domain.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Row of the {@code rm_task} read model, kept in sync with the aggregate inside the command transaction.
 */
public record TaskProjection(
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
}
