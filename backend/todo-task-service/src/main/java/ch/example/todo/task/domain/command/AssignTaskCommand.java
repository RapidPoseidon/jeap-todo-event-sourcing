package ch.example.todo.task.domain.command;

import ch.example.eventsourcing.domain.command.Command;
import ch.example.todo.task.domain.AggregateType;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class AssignTaskCommand extends Command {

    private final String assignee;

    public AssignTaskCommand(UUID aggregateId, String initiatedBy, String assignee) {
        super(AggregateType.TASK.name(), aggregateId, initiatedBy);
        this.assignee = assignee;
    }
}
