package ch.example.todo.task.adapter.web.dto;

import ch.example.todo.task.adapter.persistence.TaskStatistics;

import java.time.OffsetDateTime;

public record TaskStatisticsResponse(
        String owner,
        long createdCount,
        long completedCount,
        long reopenedCount,
        long deletedCount,
        OffsetDateTime lastUpdated) {

    public static TaskStatisticsResponse of(TaskStatistics statistics) {
        return new TaskStatisticsResponse(statistics.owner(), statistics.createdCount(), statistics.completedCount(),
                statistics.reopenedCount(), statistics.deletedCount(), statistics.lastUpdated());
    }
}
