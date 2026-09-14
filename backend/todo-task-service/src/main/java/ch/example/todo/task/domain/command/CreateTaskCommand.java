package ch.example.todo.task.domain.command;

import ch.example.eventsourcing.domain.command.Command;
import ch.example.todo.task.domain.AggregateType;
import ch.example.todo.task.domain.TaskPriority;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateTaskCommand extends Command {

    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final LocalDate dueDate;
    private final String owner;

    public CreateTaskCommand(String initiatedBy,
                             String title,
                             String description,
                             TaskPriority priority,
                             LocalDate dueDate,
                             String owner) {
        super(AggregateType.TASK.name(), UUID.randomUUID(), initiatedBy);
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.owner = owner;
    }
}
