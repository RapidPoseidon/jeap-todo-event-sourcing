package ch.example.eventsourcing.service;

import ch.example.eventsourcing.config.EventSourcingProperties;
import ch.example.eventsourcing.config.EventSourcingProperties.SnapshottingProperties;
import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.error.OptimisticConcurrencyControlException;
import ch.example.eventsourcing.repository.AggregateRepository;
import ch.example.eventsourcing.repository.EventRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Slf4j
public class AggregateStore {

    private final AggregateRepository aggregateRepository;
    private final EventRepository eventRepository;
    private final AggregateFactory aggregateFactory;
    private final EventSourcingProperties properties;

    public List<EventWithId<Event>> saveAggregate(Aggregate aggregate) {
        String aggregateType = aggregate.getAggregateType();
        UUID aggregateId = aggregate.getAggregateId();
        aggregateRepository.createAggregateIfAbsent(aggregateType, aggregateId);

        int expectedVersion = aggregate.getBaseVersion();
        if (!aggregateRepository.checkAndUpdateAggregateVersion(aggregateId, expectedVersion, aggregate.getVersion())) {
            log.warn("Optimistic concurrency control error in aggregate {} {}: expected version {}",
                    aggregateType, aggregateId, expectedVersion);
            throw new OptimisticConcurrencyControlException(expectedVersion);
        }

        SnapshottingProperties snapshotting = properties.getSnapshotting(aggregateType);
        List<EventWithId<Event>> newEvents = new ArrayList<>();
        for (Event event : aggregate.getChanges()) {
            log.info("Appending {} event: {}", aggregateType, event);
            newEvents.add(eventRepository.appendEvent(event));
            createAggregateSnapshot(snapshotting, aggregate);
        }
        return newEvents;
    }

    public Aggregate readAggregate(String aggregateType, UUID aggregateId) {
        return readAggregate(aggregateType, aggregateId, null);
    }

    public Aggregate readAggregate(@NonNull String aggregateType, @NonNull UUID aggregateId, Integer version) {
        SnapshottingProperties snapshotting = properties.getSnapshotting(aggregateType);
        if (!snapshotting.enabled()) {
            return readAggregateFromEvents(aggregateType, aggregateId, version);
        }
        return readAggregateFromSnapshot(aggregateId, version)
                .orElseGet(() -> readAggregateFromEvents(aggregateType, aggregateId, version));
    }

    private void createAggregateSnapshot(SnapshottingProperties snapshotting, Aggregate aggregate) {
        if (snapshotting.enabled()
                && snapshotting.nthEvent() > 1
                && aggregate.getVersion() % snapshotting.nthEvent() == 0) {
            log.info("Creating {} aggregate {} version {} snapshot",
                    aggregate.getAggregateType(), aggregate.getAggregateId(), aggregate.getVersion());
            aggregateRepository.createAggregateSnapshot(aggregate);
        }
    }

    private Optional<Aggregate> readAggregateFromSnapshot(UUID aggregateId, Integer aggregateVersion) {
        return aggregateRepository.readAggregateSnapshot(aggregateId, aggregateVersion)
                .map(aggregate -> {
                    int snapshotVersion = aggregate.getVersion();
                    if (aggregateVersion == null || snapshotVersion < aggregateVersion) {
                        aggregate.loadFromHistory(eventRepository
                                .readEvents(aggregateId, snapshotVersion, aggregateVersion)
                                .stream()
                                .map(EventWithId::event)
                                .toList());
                    }
                    return aggregate;
                });
    }

    private Aggregate readAggregateFromEvents(String aggregateType, UUID aggregateId, Integer aggregateVersion) {
        List<Event> events = eventRepository.readEvents(aggregateId, null, aggregateVersion)
                .stream()
                .map(EventWithId::event)
                .toList();
        Aggregate aggregate = aggregateFactory.newInstance(aggregateType, aggregateId);
        aggregate.loadFromHistory(events);
        return aggregate;
    }
}
