package ch.example.todo.task.domain.command;

import ch.example.eventsourcing.domain.command.Command;
import ch.example.todo.task.domain.AggregateType;
import lombok.ToString;

import java.util.UUID;

@ToString(callSuper = true)
public class DeleteTaskCommand extends Command {

    public DeleteTaskCommand(UUID aggregateId, String initiatedBy) {
        super(AggregateType.TASK.name(), aggregateId, initiatedBy);
    }
}
