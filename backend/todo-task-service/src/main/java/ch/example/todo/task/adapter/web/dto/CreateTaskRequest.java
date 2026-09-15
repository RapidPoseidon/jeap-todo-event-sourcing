package ch.example.todo.task.adapter.web.dto;

import ch.example.todo.task.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        TaskPriority priority,
        LocalDate dueDate) {
}
