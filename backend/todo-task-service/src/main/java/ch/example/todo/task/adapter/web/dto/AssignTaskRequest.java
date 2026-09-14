package ch.example.todo.task.adapter.web.dto;

import jakarta.validation.constraints.Size;

public record AssignTaskRequest(@Size(max = 200) String assignee) {
}
