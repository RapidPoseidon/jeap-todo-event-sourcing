package ch.example.todo.task.application.event;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.service.event.AsyncEventHandler;
import ch.example.todo.task.adapter.persistence.TaskProjectionRepository;
import ch.example.todo.task.adapter.persistence.TaskStatisticsRepository;
import ch.example.todo.task.adapter.persistence.TaskStatisticsRepository.Counter;
import ch.example.todo.task.domain.AggregateType;
import ch.example.todo.task.domain.event.TodoTaskCompletedEvent;
import ch.example.todo.task.domain.event.TodoTaskCreatedEvent;
import ch.example.todo.task.domain.event.TodoTaskDeletedEvent;
import ch.example.todo.task.domain.event.TodoTaskReopenedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Counts lifecycle events per owner from the committed event stream; these totals cannot be derived from current state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskStatisticsUpdater implements AsyncEventHandler {

    private final TaskStatisticsRepository statisticsRepository;
    private final TaskProjectionRepository taskRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void handleEvent(EventWithId<Event> eventWithId) {
        Event event = eventWithId.event();
        switch (event) {
            case TodoTaskCreatedEvent created -> statisticsRepository.increment(created.getOwner(), Counter.CREATED);
            case TodoTaskCompletedEvent completed -> increment(completed, Counter.COMPLETED);
            case TodoTaskReopenedEvent reopened -> increment(reopened, Counter.REOPENED);
            case TodoTaskDeletedEvent deleted -> increment(deleted, Counter.DELETED);
            default -> log.debug("No statistics kept for event type {}", event.getEventType());
        }
    }

    @Override
    public String getAggregateType() {
        return AggregateType.TASK.name();
    }

    private void increment(Event event, Counter counter) {
        taskRepository.findById(event.getAggregateId())
                .ifPresent(task -> statisticsRepository.increment(task.owner(), counter));
    }
}
