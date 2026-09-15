package ch.example.eventsourcing.domain;

/**
 * Maps the aggregate type persisted in the event store to the aggregate class to deserialize into.
 */
public interface AggregateTypeMapper {

    Class<? extends Aggregate> getClassByAggregateType(String aggregateType);
}
