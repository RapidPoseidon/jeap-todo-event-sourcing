package ch.example.todo.task.application;

import ch.example.eventsourcing.service.CommandProcessor;
import ch.example.todo.task.adapter.persistence.TaskProjectionRepository;
import ch.example.todo.task.domain.TaskAggregate;
import ch.example.todo.task.domain.TaskPriority;
import ch.example.todo.task.domain.command.AssignTaskCommand;
import ch.example.todo.task.domain.command.ChangeTaskDetailsCommand;
import ch.example.todo.task.domain.command.CompleteTaskCommand;
import ch.example.todo.task.domain.command.CreateTaskCommand;
import ch.example.todo.task.domain.command.DeleteTaskCommand;
import ch.example.todo.task.domain.command.ReopenTaskCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Turns an authenticated request into a command and hands it to the event-sourcing command processor.
 */
@Service
@RequiredArgsConstructor
public class TaskCommandService {

    private final CommandProcessor commandProcessor;
    private final TaskProjectionRepository taskRepository;
    private final CurrentUser currentUser;

    public TaskAggregate createTask(String title, String description, TaskPriority priority, LocalDate dueDate) {
        String user = currentUser.name();
        return (TaskAggregate) commandProcessor.process(
                new CreateTaskCommand(user, title, description, priority, dueDate, user));
    }

    public TaskAggregate changeDetails(UUID taskId, String title, String description,
                                       TaskPriority priority, LocalDate dueDate) {
        requireOwnership(taskId);
        return (TaskAggregate) commandProcessor.process(
                new ChangeTaskDetailsCommand(taskId, currentUser.name(), title, description, priority, dueDate));
    }

    public TaskAggregate assign(UUID taskId, String assignee) {
        requireOwnership(taskId);
        return (TaskAggregate) commandProcessor.process(new AssignTaskCommand(taskId, currentUser.name(), assignee));
    }

    public TaskAggregate complete(UUID taskId) {
        requireOwnership(taskId);
        return (TaskAggregate) commandProcessor.process(new CompleteTaskCommand(taskId, currentUser.name()));
    }

    public TaskAggregate reopen(UUID taskId) {
        requireOwnership(taskId);
        return (TaskAggregate) commandProcessor.process(new ReopenTaskCommand(taskId, currentUser.name()));
    }

    public TaskAggregate delete(UUID taskId) {
        requireOwnership(taskId);
        return (TaskAggregate) commandProcessor.process(new DeleteTaskCommand(taskId, currentUser.name()));
    }

    private void requireOwnership(UUID taskId) {
        String owner = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task %s not found".formatted(taskId)))
                .owner();
        if (!currentUser.name().equals(owner)) {
            throw new AccessDeniedException("Task %s belongs to another user".formatted(taskId));
        }
    }
}
