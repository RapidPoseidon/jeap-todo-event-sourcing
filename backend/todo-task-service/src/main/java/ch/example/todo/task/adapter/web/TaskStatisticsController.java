package ch.example.todo.task.adapter.web;

import ch.example.todo.task.adapter.web.dto.TaskStatisticsResponse;
import ch.example.todo.task.application.TaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Counters maintained by the asynchronous event subscription")
public class TaskStatisticsController {

    private final TaskQueryService queryService;

    @GetMapping
    @PreAuthorize("hasRole('task', 'read')")
    @Operation(summary = "Returns lifecycle counters; eventually consistent with the event stream")
    public TaskStatisticsResponse findStatistics() {
        return TaskStatisticsResponse.of(queryService.findStatistics());
    }
}
