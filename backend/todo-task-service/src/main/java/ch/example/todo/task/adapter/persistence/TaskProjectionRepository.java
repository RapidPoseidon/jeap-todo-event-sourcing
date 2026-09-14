package ch.example.todo.task.adapter.persistence;

import ch.example.todo.task.domain.TaskPriority;
import ch.example.todo.task.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TaskProjectionRepository {

    private static final String SELECT = """
            SELECT ID, VERSION, STATUS, TITLE, DESCRIPTION, PRIORITY, DUE_DATE, OWNER, ASSIGNEE,
                   CREATED_BY, CREATED_DATE, LAST_MODIFIED_DATE, COMPLETED_DATE, COMPLETION_COUNT
              FROM RM_TASK
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Replays are idempotent and the projection is always derived from the whole aggregate, so an upsert is
     * the correct write: the read model converges to the aggregate state no matter how often it is applied.
     */
    public void save(TaskProjection task) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", task.id())
                .addValue("version", task.version())
                .addValue("status", task.status().name())
                .addValue("title", task.title())
                .addValue("description", task.description())
                .addValue("priority", task.priority().name())
                .addValue("dueDate", task.dueDate())
                .addValue("owner", task.owner())
                .addValue("assignee", task.assignee())
                .addValue("createdBy", task.createdBy())
                .addValue("createdDate", task.createdDate())
                .addValue("lastModifiedDate", task.lastModifiedDate())
                .addValue("completedDate", task.completedDate(), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("completionCount", task.completionCount());

        jdbcTemplate.update("""
                INSERT INTO RM_TASK (ID, VERSION, STATUS, TITLE, DESCRIPTION, PRIORITY, DUE_DATE, OWNER, ASSIGNEE,
                                     CREATED_BY, CREATED_DATE, LAST_MODIFIED_DATE, COMPLETED_DATE, COMPLETION_COUNT)
                VALUES (:id, :version, :status, :title, :description, :priority, :dueDate, :owner, :assignee,
                        :createdBy, :createdDate, :lastModifiedDate, :completedDate, :completionCount)
                ON CONFLICT (ID) DO UPDATE
                   SET VERSION = EXCLUDED.VERSION,
                       STATUS = EXCLUDED.STATUS,
                       TITLE = EXCLUDED.TITLE,
                       DESCRIPTION = EXCLUDED.DESCRIPTION,
                       PRIORITY = EXCLUDED.PRIORITY,
                       DUE_DATE = EXCLUDED.DUE_DATE,
                       OWNER = EXCLUDED.OWNER,
                       ASSIGNEE = EXCLUDED.ASSIGNEE,
                       LAST_MODIFIED_DATE = EXCLUDED.LAST_MODIFIED_DATE,
                       COMPLETED_DATE = EXCLUDED.COMPLETED_DATE,
                       COMPLETION_COUNT = EXCLUDED.COMPLETION_COUNT
                 WHERE RM_TASK.VERSION < EXCLUDED.VERSION
                """, parameters);
    }

    public Optional<TaskProjection> findById(UUID id) {
        return jdbcTemplate.query(SELECT + " WHERE ID = :id", Map.of("id", id), this::toProjection)
                .stream()
                .findFirst();
    }

    public List<TaskProjection> find(String owner, TaskStatus status) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("status", status == null ? null : status.name(), Types.VARCHAR);

        return jdbcTemplate.query(SELECT + """
                 WHERE OWNER = :owner
                   AND STATUS <> 'DELETED'
                   AND (:status IS NULL OR STATUS = :status)
                 ORDER BY CASE PRIORITY WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END,
                          DUE_DATE ASC NULLS LAST,
                          CREATED_DATE DESC
                """, parameters, this::toProjection);
    }

    private TaskProjection toProjection(ResultSet rs, int rowNum) throws SQLException {
        LocalDate dueDate = rs.getObject("DUE_DATE", LocalDate.class);
        return new TaskProjection(
                rs.getObject("ID", UUID.class),
                rs.getInt("VERSION"),
                TaskStatus.valueOf(rs.getString("STATUS")),
                rs.getString("TITLE"),
                rs.getString("DESCRIPTION"),
                TaskPriority.valueOf(rs.getString("PRIORITY")),
                dueDate,
                rs.getString("OWNER"),
                rs.getString("ASSIGNEE"),
                rs.getString("CREATED_BY"),
                rs.getObject("CREATED_DATE", OffsetDateTime.class),
                rs.getObject("LAST_MODIFIED_DATE", OffsetDateTime.class),
                rs.getObject("COMPLETED_DATE", OffsetDateTime.class),
                rs.getInt("COMPLETION_COUNT"));
    }
}
