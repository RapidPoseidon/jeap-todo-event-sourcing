package ch.example.eventsourcing.service;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.command.Command;
import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.domain.event.EventWithId;
import ch.example.eventsourcing.service.command.CommandHandler;
import ch.example.eventsourcing.service.command.DefaultCommandHandler;
import ch.example.eventsourcing.service.event.SyncEventHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor {

    private final AggregateStore aggregateStore;
    private final List<CommandHandler<? extends Command>> commandHandlers;
    private final DefaultCommandHandler defaultCommandHandler;
    private final List<SyncEventHandler> syncEventHandlers;

    public Aggregate process(@NonNull Command command) {
        log.debug("Processing command {}", command);
        String aggregateType = command.getAggregateType();
        Aggregate aggregate = aggregateStore.readAggregate(aggregateType, command.getAggregateId());

        commandHandlers.stream()
                .filter(commandHandler -> commandHandler.getCommandType() == command.getClass())
                .findFirst()
                .orElse(defaultCommandHandler)
                .handle(aggregate, command);

        List<EventWithId<Event>> newEvents = aggregateStore.saveAggregate(aggregate);

        syncEventHandlers.stream()
                .filter(handler -> handler.getAggregateType().equals(aggregateType))
                .forEach(handler -> handler.handleEvents(newEvents, aggregate));

        return aggregate;
    }
}
