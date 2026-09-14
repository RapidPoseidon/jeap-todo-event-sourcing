package ch.example.todo.task.domain;

import ch.example.eventsourcing.domain.event.Event;
import ch.example.eventsourcing.error.AggregateStateException;
import ch.example.todo.task.domain.command.CompleteTaskCommand;
import ch.example.todo.task.domain.command.CreateTaskCommand;
import ch.example.todo.task.domain.command.DeleteTaskCommand;
import ch.example.todo.task.domain.command.ReopenTaskCommand;
import ch.example.todo.task.domain.event.TodoTaskCompletedEvent;
import ch.example.todo.task.domain.event.TodoTaskCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskAggregateTest {

    private static final String USER = "anna";

    @Test
    void createTask_producesCreatedEventAtVersionOne() {
        TaskAggregate task = newTask();

        task.process(new CreateTaskCommand(USER, "Write the RFC", "Draft it", TaskPriority.HIGH,
                LocalDate.of(2026, 9, 30), USER));

        assertThat(task.getVersion()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(task.getChanges()).singleElement().isInstanceOf(TodoTaskCreatedEvent.class);
        assertThat(task.getChanges().getFirst().getInitiatedBy()).isEqualTo(USER);
    }

    @Test
    void createTask_withoutTitle_isRejected() {
        TaskAggregate task = newTask();

        assertThatThrownBy(() -> task.process(
                new CreateTaskCommand(USER, "  ", null, TaskPriority.LOW, null, USER)))
                .isInstanceOf(AggregateStateException.class)
                .hasMessageContaining("title");
    }

    @Test
    void completeTask_twice_isRejected() {
        TaskAggregate task = newTask();
        task.process(new CreateTaskCommand(USER, "Write the RFC", null, TaskPriority.LOW, null, USER));
        task.process(new CompleteTaskCommand(task.getAggregateId(), USER));

        assertThatThrownBy(() -> task.process(new CompleteTaskCommand(task.getAggregateId(), USER)))
                .isInstanceOf(AggregateStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    void reopenTask_countsCompletionsOverTime() {
        TaskAggregate task = newTask();
        task.process(new CreateTaskCommand(USER, "Write the RFC", null, TaskPriority.LOW, null, USER));
        task.process(new CompleteTaskCommand(task.getAggregateId(), USER));
        task.process(new ReopenTaskCommand(task.getAggregateId(), USER));
        task.process(new CompleteTaskCommand(task.getAggregateId(), USER));

        assertThat(task.getCompletionCount()).isEqualTo(2);
        assertThat(task.getVersion()).isEqualTo(4);
    }

    @Test
    void deleteTask_twice_isRejected() {
        TaskAggregate task = newTask();
        task.process(new CreateTaskCommand(USER, "Write the RFC", null, TaskPriority.LOW, null, USER));
        task.process(new DeleteTaskCommand(task.getAggregateId(), USER));

        assertThatThrownBy(() -> task.process(new DeleteTaskCommand(task.getAggregateId(), USER)))
                .isInstanceOf(AggregateStateException.class);
    }

    @Test
    void loadFromHistory_rebuildsTheSameState() {
        TaskAggregate source = newTask();
        source.process(new CreateTaskCommand(USER, "Write the RFC", "Draft it", TaskPriority.HIGH, null, USER));
        source.process(new CompleteTaskCommand(source.getAggregateId(), USER));
        List<Event> history = List.copyOf(source.getChanges());

        TaskAggregate replayed = new TaskAggregate(source.getAggregateId(), 0);
        replayed.loadFromHistory(history);

        assertThat(replayed.getVersion()).isEqualTo(source.getVersion());
        assertThat(replayed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(replayed.getTitle()).isEqualTo("Write the RFC");
        assertThat(replayed.getChanges()).isEmpty();
    }

    @Test
    void loadFromHistory_keepsTheOriginalEventTimestamp() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime created = OffsetDateTime.parse("2026-01-02T03:04:05Z");
        TaskAggregate task = new TaskAggregate(aggregateId, 0);

        task.loadFromHistory(List.of(
                TodoTaskCreatedEvent.builder()
                        .aggregateId(aggregateId)
                        .version(1)
                        .createdDate(created)
                        .initiatedBy(USER)
                        .title("Write the RFC")
                        .priority(TaskPriority.LOW)
                        .owner(USER)
                        .build(),
                TodoTaskCompletedEvent.builder()
                        .aggregateId(aggregateId)
                        .version(2)
                        .createdDate(created.plusHours(1))
                        .initiatedBy(USER)
                        .build()));

        assertThat(task.getCreatedDate()).isEqualTo(created);
        assertThat(task.getCompletedDate()).isEqualTo(created.plusHours(1));
    }

    private TaskAggregate newTask() {
        return new TaskAggregate(UUID.randomUUID(), 0);
    }
}
