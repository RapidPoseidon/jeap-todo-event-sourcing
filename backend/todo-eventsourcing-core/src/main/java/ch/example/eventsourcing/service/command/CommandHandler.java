package ch.example.eventsourcing.service.command;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.command.Command;

public interface CommandHandler<T extends Command> {

    void handle(Aggregate aggregate, Command command);

    Class<T> getCommandType();
}
