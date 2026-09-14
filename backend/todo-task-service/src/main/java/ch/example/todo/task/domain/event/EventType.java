package ch.example.todo.task.domain.event;

import ch.example.eventsourcing.domain.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {

    TODO_TASK_CREATED(TodoTaskCreatedEvent.class),
    TODO_TASK_DETAILS_CHANGED(TodoTaskDetailsChangedEvent.class),
    TODO_TASK_ASSIGNED(TodoTaskAssignedEvent.class),
    TODO_TASK_COMPLETED(TodoTaskCompletedEvent.class),
    TODO_TASK_REOPENED(TodoTaskReopenedEvent.class),
    TODO_TASK_DELETED(TodoTaskDeletedEvent.class);

    private final Class<? extends Event> eventClass;
}
