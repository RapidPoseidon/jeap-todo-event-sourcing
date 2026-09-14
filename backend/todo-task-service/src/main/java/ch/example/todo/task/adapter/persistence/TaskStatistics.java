package ch.example.todo.task.adapter.persistence;

import java.time.OffsetDateTime;

/**
 * Row of the {@code rm_task_statistics} read model: counters that only the event stream can answer.
 */
public record TaskStatistics(
        String owner,
        long createdCount,
        long completedCount,
        long reopenedCount,
        long deletedCount,
        OffsetDateTime lastUpdated) {
}
