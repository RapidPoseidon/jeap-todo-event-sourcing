package ch.example.todo.task.adapter.web.dto;

import ch.example.todo.task.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull TaskStatus status) {
}
