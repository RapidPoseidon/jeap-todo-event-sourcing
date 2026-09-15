package ch.example.todo.task.application.event;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.service.event.SyncEventHandler;
import ch.example.todo.task.adapter.persistence.TaskProjection;
import ch.example.todo.task.adapter.persistence.TaskProjectionRepository;
import ch.example.todo.task.domain.AggregateType;
import ch.example.todo.task.domain.TaskAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Keeps {@code rm_task} in the same transaction as the events, so a command's own response never reads stale data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskProjectionUpdater implements SyncEventHandler {

    private final TaskProjectionRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {
        TaskAggregate task = (TaskAggregate) aggregate;
        log.debug("Updating read model for task {} version {}", task.getAggregateId(), task.getVersion());
        repository.save(new TaskProjection(
                task.getAggregateId(),
                task.getVersion(),
                task.getStatus(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getOwner(),
                task.getAssignee(),
                task.getCreatedBy(),
                task.getCreatedDate(),
                task.getLastModifiedDate(),
                task.getCompletedDate(),
                task.getCompletionCount()));
    }

    @Override
    public String getAggregateType() {
        return AggregateType.TASK.name();
    }
}
