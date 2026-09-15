package ch.example.eventsourcing.service;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.AggregateTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.UUID;

@RequiredArgsConstructor
public class AggregateFactory {

    private final AggregateTypeMapper aggregateTypeMapper;

    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    public <T extends Aggregate> T newInstance(String aggregateType, UUID aggregateId) {
        Class<? extends Aggregate> aggregateClass = aggregateTypeMapper.getClassByAggregateType(aggregateType);
        return (T) aggregateClass.getDeclaredConstructor(UUID.class, Integer.TYPE).newInstance(aggregateId, 0);
    }
}
