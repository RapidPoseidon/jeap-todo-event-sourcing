package ch.example.eventsourcing.service.command;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.domain.command.Command;

/**
 * Handles commands that need no orchestration beyond the aggregate's own decision.
 */
public class DefaultCommandHandler implements CommandHandler<Command> {

    @Override
    public void handle(Aggregate aggregate, Command command) {
        aggregate.process(command);
    }

    @Override
    public Class<Command> getCommandType() {
        return Command.class;
    }
}
