package ch.example.todo.task.adapter.web;

import ch.example.todo.task.adapter.web.dto.AssignTaskRequest;
import ch.example.todo.task.adapter.web.dto.ChangeTaskDetailsRequest;
import ch.example.todo.task.adapter.web.dto.ChangeTaskStatusRequest;
import ch.example.todo.task.adapter.web.dto.CreateTaskRequest;
import ch.example.todo.task.adapter.web.dto.TaskEventResponse;
import ch.example.todo.task.adapter.web.dto.TaskResponse;
import ch.example.todo.task.application.TaskCommandService;
import ch.example.todo.task.application.TaskQueryService;
import ch.example.todo.task.domain.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Commands and queries of the task bounded context")
public class TaskController {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @PreAuthorize("hasRole('task', 'write')")
    @Operation(summary = "Creates a task owned by the authenticated user")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        TaskResponse task = TaskResponse.of(commandService.createTask(
                request.title(), request.description(), request.priority(), request.dueDate()));
        URI location = uriBuilder.path("/api/tasks/{id}").build(task.id());
        return ResponseEntity.created(location).body(task);
    }

    @GetMapping
    @PreAuthorize("hasRole('task', 'read')")
    @Operation(summary = "Lists the authenticated user's tasks, newest and most urgent first")
    public List<TaskResponse> findTasks(@RequestParam(required = false) TaskStatus status) {
        return queryService.findTasks(status).stream().map(TaskResponse::of).toList();
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('task', 'read')")
    public TaskResponse findTask(@PathVariable UUID taskId) {
        return TaskResponse.of(queryService.findTask(taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('task', 'write')")
    public TaskResponse changeDetails(@PathVariable UUID taskId,
                                      @Valid @RequestBody ChangeTaskDetailsRequest request) {
        return TaskResponse.of(commandService.changeDetails(
                taskId, request.title(), request.description(), request.priority(), request.dueDate()));
    }

    @PutMapping("/{taskId}/assignee")
    @PreAuthorize("hasRole('task', 'write')")
    public TaskResponse assign(@PathVariable UUID taskId, @Valid @RequestBody AssignTaskRequest request) {
        return TaskResponse.of(commandService.assign(taskId, request.assignee()));
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasRole('task', 'write')")
    @Operation(summary = "Completes or reopens a task")
    public TaskResponse changeStatus(@PathVariable UUID taskId, @Valid @RequestBody ChangeTaskStatusRequest request) {
        return TaskResponse.of(switch (request.status()) {
            case COMPLETED -> commandService.complete(taskId);
            case OPEN -> commandService.reopen(taskId);
            case DELETED -> commandService.delete(taskId);
        });
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('task', 'write')")
    public ResponseEntity<Void> delete(@PathVariable UUID taskId) {
        commandService.delete(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/events")
    @PreAuthorize("hasRole('task', 'read')")
    @Operation(summary = "Returns the task's full event history, oldest first")
    public List<TaskEventResponse> findHistory(@PathVariable UUID taskId) {
        return queryService.findTaskHistory(taskId).stream()
                .map(event -> TaskEventResponse.of(event, objectMapper))
                .toList();
    }

    @GetMapping("/{taskId}/versions/{version}")
    @PreAuthorize("hasRole('task', 'read')")
    @Operation(summary = "Replays the task as it was at the given version")
    public TaskResponse findAtVersion(@PathVariable UUID taskId, @PathVariable int version) {
        return TaskResponse.of(queryService.findTaskAtVersion(taskId, version));
    }
}
