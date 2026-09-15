package ch.example.todo.task.domain;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.AggregateTypeMapper;
import org.springframework.stereotype.Component;

@Component
public class DefaultAggregateTypeMapper implements AggregateTypeMapper {

    @Override
    public Class<? extends Aggregate> getClassByAggregateType(String aggregateType) {
        return AggregateType.valueOf(aggregateType).getAggregateClass();
    }
}
