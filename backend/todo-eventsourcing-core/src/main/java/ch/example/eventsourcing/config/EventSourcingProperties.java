package ch.example.eventsourcing.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "event-sourcing")
public class EventSourcingProperties {

    private static final SnapshottingProperties NO_SNAPSHOTTING = new SnapshottingProperties(false, 0);

    @Valid
    @NestedConfigurationProperty
    @Setter
    private Map<String, SnapshottingProperties> snapshotting = new HashMap<>();

    @Getter
    @Setter
    private Subscriptions subscriptions = Subscriptions.POSTGRES_CHANNEL;

    @Getter
    @Setter
    @Valid
    @NestedConfigurationProperty
    private PollingSubscriptionsProperties pollingSubscriptions = new PollingSubscriptionsProperties();

    public SnapshottingProperties getSnapshotting(String aggregateType) {
        return snapshotting.getOrDefault(aggregateType, NO_SNAPSHOTTING);
    }

    public enum Subscriptions {
        POLLING,
        POSTGRES_CHANNEL
    }

    public record SnapshottingProperties(boolean enabled, @Min(2) int nthEvent) {
    }

    @Getter
    @Setter
    public static class PollingSubscriptionsProperties {

        private Duration pollingInitialDelay = Duration.ofSeconds(1);
        private Duration pollingInterval = Duration.ofSeconds(1);
    }
}
