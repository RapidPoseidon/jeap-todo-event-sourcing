package ch.example.todo.task.domain;

import ch.example.eventsourcing.domain.Aggregate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AggregateType {

    TASK(TaskAggregate.class);

    private final Class<? extends Aggregate> aggregateClass;
}
