package ch.example.eventsourcing.service;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.repository.EventRepository;
import ch.example.eventsourcing.repository.EventSubscriptionRepository;
import ch.example.eventsourcing.service.event.AsyncEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
@Slf4j
public class EventSubscriptionProcessor {

    private final EventSubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;

    @Async
    public void processNewEvents(AsyncEventHandler eventHandler) {
        String subscriptionName = eventHandler.getSubscriptionName();
        subscriptionRepository.createSubscriptionIfAbsent(subscriptionName);
        subscriptionRepository.readCheckpointAndLockSubscription(subscriptionName).ifPresentOrElse(
                checkpoint -> {
                    List<EventWithId<Event>> events = eventRepository.readEventsAfterCheckpoint(
                            eventHandler.getAggregateType(),
                            checkpoint.lastProcessedTransactionId(),
                            checkpoint.lastProcessedEventId());
                    log.debug("Fetched {} new event(s) for subscription {}", events.size(), subscriptionName);
                    if (!events.isEmpty()) {
                        events.forEach(eventHandler::handleEvent);
                        EventWithId<Event> lastEvent = events.getLast();
                        subscriptionRepository.updateEventSubscription(
                                subscriptionName, lastEvent.transactionId(), lastEvent.id());
                    }
                },
                () -> log.debug("Can't acquire lock on subscription {}", subscriptionName));
    }
}
