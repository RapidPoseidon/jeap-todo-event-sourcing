package ch.example.todo.task.application;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.repository.EventRepository;
import ch.example.eventsourcing.service.AggregateStore;
import ch.example.todo.task.adapter.persistence.TaskProjection;
import ch.example.todo.task.adapter.persistence.TaskProjectionRepository;
import ch.example.todo.task.adapter.persistence.TaskStatistics;
import ch.example.todo.task.adapter.persistence.TaskStatisticsRepository;
import ch.example.todo.task.domain.AggregateType;
import ch.example.todo.task.domain.TaskAggregate;
import ch.example.todo.task.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskQueryService {

    private final TaskProjectionRepository taskRepository;
    private final TaskStatisticsRepository statisticsRepository;
    private final EventRepository eventRepository;
    private final AggregateStore aggregateStore;
    private final CurrentUser currentUser;

    public List<TaskProjection> findTasks(TaskStatus status) {
        return taskRepository.find(currentUser.name(), status);
    }

    public TaskProjection findTask(UUID taskId) {
        TaskProjection task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task %s not found".formatted(taskId)));
        requireOwnership(task);
        return task;
    }

    public List<EventWithId<Event>> findTaskHistory(UUID taskId) {
        requireOwnership(findTask(taskId));
        return eventRepository.readEvents(taskId, null, null);
    }

    /**
     * Replays the aggregate up to {@code version}, which is what an event-sourced model can answer and a CRUD one can't.
     */
    public TaskAggregate findTaskAtVersion(UUID taskId, int version) {
        requireOwnership(findTask(taskId));
        return (TaskAggregate) aggregateStore.readAggregate(AggregateType.TASK.name(), taskId, version);
    }

    public TaskStatistics findStatistics() {
        String owner = currentUser.name();
        return statisticsRepository.findByOwner(owner)
                .orElseGet(() -> new TaskStatistics(owner, 0, 0, 0, 0, OffsetDateTime.now()));
    }

    private void requireOwnership(TaskProjection task) {
        if (!currentUser.name().equals(task.owner())) {
            throw new AccessDeniedException("Task %s belongs to another user".formatted(task.id()));
        }
    }
}
