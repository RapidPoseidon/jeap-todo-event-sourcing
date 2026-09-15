package ch.example.eventsourcing.domain;

import ch.example.eventsourcing.domain.command.Command;
import ch.example.eventsourcing.domain.event.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base class of an event-sourced aggregate root; state changes only by applying events.
 * Concrete aggregates contribute {@code process(SomeCommand)} / {@code apply(SomeEvent)} methods, dispatched reflectively.
 */
@Getter
@ToString
@Slf4j
public abstract class Aggregate {

    protected final UUID aggregateId;

    @JsonIgnore
    protected final List<Event> changes = new ArrayList<>();

    protected int version;

    @JsonIgnore
    protected int baseVersion;

    protected Aggregate(@NonNull UUID aggregateId, int version) {
        this.aggregateId = aggregateId;
        this.baseVersion = this.version = version;
    }

    public abstract String getAggregateType();

    public void loadFromHistory(List<Event> events) {
        if (!changes.isEmpty()) {
            throw new IllegalStateException("Aggregate has non-empty changes");
        }
        events.forEach(event -> {
            if (event.getVersion() <= version) {
                throw new IllegalStateException("Event version %s <= aggregate version %s"
                        .formatted(event.getVersion(), version));
            }
            apply(event);
            baseVersion = version = event.getVersion();
        });
    }

    public void process(Command command) {
        log.debug("Processing command {}", command);
        invoke(command, "process");
    }

    protected int getNextVersion() {
        return version + 1;
    }

    protected void applyChange(Event event) {
        if (event.getVersion() != getNextVersion()) {
            throw new IllegalStateException("Event version %s doesn't match expected version %s"
                    .formatted(event.getVersion(), getNextVersion()));
        }
        apply(event);
        changes.add(event);
        version = event.getVersion();
    }

    private void apply(Event event) {
        log.debug("Applying event {}", event);
        invoke(event, "apply");
    }

    @SneakyThrows(InvocationTargetException.class)
    private void invoke(Object argument, String methodName) {
        try {
            Method method = getClass().getMethod(methodName, argument.getClass());
            method.invoke(this, argument);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnsupportedOperationException("Aggregate %s doesn't support %s(%s)"
                    .formatted(getClass(), methodName, argument.getClass().getSimpleName()), e);
        }
    }
}
