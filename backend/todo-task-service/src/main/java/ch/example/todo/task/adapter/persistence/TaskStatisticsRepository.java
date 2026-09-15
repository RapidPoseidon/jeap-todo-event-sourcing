package ch.example.todo.task.adapter.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskStatisticsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void increment(String owner, Counter counter) {
        String counterColumn = counter.getColumn();
        jdbcTemplate.update("""
                INSERT INTO RM_TASK_STATISTICS (OWNER, %s, LAST_UPDATED)
                VALUES (:owner, 1, now())
                ON CONFLICT (OWNER) DO UPDATE
                   SET %s = RM_TASK_STATISTICS.%s + 1,
                       LAST_UPDATED = now()
                """.formatted(counterColumn, counterColumn, counterColumn),
                Map.of("owner", owner));
    }

    public Optional<TaskStatistics> findByOwner(String owner) {
        return jdbcTemplate.query("""
                        SELECT OWNER, CREATED_COUNT, COMPLETED_COUNT, REOPENED_COUNT, DELETED_COUNT, LAST_UPDATED
                          FROM RM_TASK_STATISTICS
                         WHERE OWNER = :owner
                        """,
                Map.of("owner", owner),
                this::toStatistics).stream().findFirst();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Counter {

        CREATED("CREATED_COUNT"),
        COMPLETED("COMPLETED_COUNT"),
        REOPENED("REOPENED_COUNT"),
        DELETED("DELETED_COUNT");

        private final String column;
    }

    private TaskStatistics toStatistics(ResultSet rs, int rowNum) throws SQLException {
        return new TaskStatistics(
                rs.getString("OWNER"),
                rs.getLong("CREATED_COUNT"),
                rs.getLong("COMPLETED_COUNT"),
                rs.getLong("REOPENED_COUNT"),
                rs.getLong("DELETED_COUNT"),
                rs.getObject("LAST_UPDATED", OffsetDateTime.class));
    }
}
