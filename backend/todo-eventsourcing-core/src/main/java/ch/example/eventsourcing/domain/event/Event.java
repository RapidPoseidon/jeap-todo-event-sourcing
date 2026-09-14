package ch.example.eventsourcing.domain.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A fact that happened to an aggregate, identified by the aggregate id and the version it produced.
 * {@code createdDate} is a constructor parameter so that replaying an event restores the original timestamp.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public abstract class Event {

    protected final UUID aggregateId;
    protected final int version;
    protected final OffsetDateTime createdDate;
    protected final String initiatedBy;

    protected Event(UUID aggregateId, int version, OffsetDateTime createdDate) {
        this(aggregateId, version, createdDate, null);
    }

    public abstract String getEventType();
}
