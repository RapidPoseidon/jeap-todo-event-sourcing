package ch.example.todo.task.domain;

import ch.example.eventsourcing.domain.Aggregate;
import ch.example.eventsourcing.error.AggregateStateException;
import ch.example.todo.task.domain.command.AssignTaskCommand;
import ch.example.todo.task.domain.command.ChangeTaskDetailsCommand;
import ch.example.todo.task.domain.command.CompleteTaskCommand;
import ch.example.todo.task.domain.command.CreateTaskCommand;
import ch.example.todo.task.domain.command.DeleteTaskCommand;
import ch.example.todo.task.domain.command.ReopenTaskCommand;
import ch.example.todo.task.domain.event.TodoTaskAssignedEvent;
import ch.example.todo.task.domain.event.TodoTaskCompletedEvent;
import ch.example.todo.task.domain.event.TodoTaskCreatedEvent;
import ch.example.todo.task.domain.event.TodoTaskDeletedEvent;
import ch.example.todo.task.domain.event.TodoTaskDetailsChangedEvent;
import ch.example.todo.task.domain.event.TodoTaskReopenedEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The task aggregate root: the only place where a task's invariants are decided.
 */
@Getter
@ToString(callSuper = true)
public class TaskAggregate extends Aggregate {

    private static final int MAX_TITLE_LENGTH = 200;

    private TaskStatus status;
    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate dueDate;
    private String owner;
    private String assignee;
    private String createdBy;
    private OffsetDateTime createdDate;
    private OffsetDateTime lastModifiedDate;
    private OffsetDateTime completedDate;
    private int completionCount;

    @JsonCreator
    public TaskAggregate(@NonNull UUID aggregateId, int version) {
        super(aggregateId, version);
    }

    public void process(CreateTaskCommand command) {
        if (status != null) {
            throw new AggregateStateException("Task %s already exists", aggregateId);
        }
        requireValidTitle(command.getTitle());
        applyChange(TodoTaskCreatedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .title(command.getTitle())
                .description(command.getDescription())
                .priority(command.getPriority() == null ? TaskPriority.MEDIUM : command.getPriority())
                .dueDate(command.getDueDate())
                .owner(command.getOwner())
                .build());
    }

    public void process(ChangeTaskDetailsCommand command) {
        requireStatus(TaskStatus.OPEN, "change the details of");
        requireValidTitle(command.getTitle());
        applyChange(TodoTaskDetailsChangedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .title(command.getTitle())
                .description(command.getDescription())
                .priority(command.getPriority() == null ? priority : command.getPriority())
                .dueDate(command.getDueDate())
                .build());
    }

    public void process(AssignTaskCommand command) {
        requireStatus(TaskStatus.OPEN, "assign");
        applyChange(TodoTaskAssignedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .assignee(command.getAssignee())
                .build());
    }

    public void process(CompleteTaskCommand command) {
        requireStatus(TaskStatus.OPEN, "complete");
        applyChange(TodoTaskCompletedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .build());
    }

    public void process(ReopenTaskCommand command) {
        requireStatus(TaskStatus.COMPLETED, "reopen");
        applyChange(TodoTaskReopenedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .build());
    }

    public void process(DeleteTaskCommand command) {
        if (status == TaskStatus.DELETED) {
            throw new AggregateStateException("Task %s is already deleted", aggregateId);
        }
        requireExists();
        applyChange(TodoTaskDeletedEvent.builder()
                .aggregateId(aggregateId)
                .version(getNextVersion())
                .createdDate(OffsetDateTime.now())
                .initiatedBy(command.getInitiatedBy())
                .build());
    }

    public void apply(TodoTaskCreatedEvent event) {
        this.status = TaskStatus.OPEN;
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.priority = event.getPriority();
        this.dueDate = event.getDueDate();
        this.owner = event.getOwner();
        this.createdBy = event.getInitiatedBy();
        this.createdDate = event.getCreatedDate();
        this.lastModifiedDate = event.getCreatedDate();
    }

    public void apply(TodoTaskDetailsChangedEvent event) {
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.priority = event.getPriority();
        this.dueDate = event.getDueDate();
        this.lastModifiedDate = event.getCreatedDate();
    }

    public void apply(TodoTaskAssignedEvent event) {
        this.assignee = event.getAssignee();
        this.lastModifiedDate = event.getCreatedDate();
    }

    public void apply(TodoTaskCompletedEvent event) {
        this.status = TaskStatus.COMPLETED;
        this.completedDate = event.getCreatedDate();
        this.completionCount++;
        this.lastModifiedDate = event.getCreatedDate();
    }

    public void apply(TodoTaskReopenedEvent event) {
        this.status = TaskStatus.OPEN;
        this.completedDate = null;
        this.lastModifiedDate = event.getCreatedDate();
    }

    public void apply(TodoTaskDeletedEvent event) {
        this.status = TaskStatus.DELETED;
        this.lastModifiedDate = event.getCreatedDate();
    }

    @Override
    public String getAggregateType() {
        return AggregateType.TASK.name();
    }

    private void requireExists() {
        if (status == null) {
            throw new AggregateStateException("Task %s does not exist", aggregateId);
        }
    }

    private void requireStatus(TaskStatus expected, String action) {
        requireExists();
        if (status != expected) {
            throw new AggregateStateException("Can't %s a task in status %s", action, status);
        }
    }

    private void requireValidTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new AggregateStateException("A task needs a title");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new AggregateStateException("A task title is at most %s characters", MAX_TITLE_LENGTH);
        }
    }
}
