package ch.example.eventsourcing.domain.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * Intent to change one aggregate, addressed by its type and id.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public abstract class Command {

    protected final String aggregateType;
    protected final UUID aggregateId;
    protected final String initiatedBy;
}
