package ch.example.eventsourcing.config;

import ch.example.eventsourcing.domain.AggregateTypeMapper;
import ch.example.eventsourcing.domain.command.Command;
import ch.example.eventsourcing.domain.event.EventTypeMapper;
import ch.example.eventsourcing.repository.AggregateRepository;
import ch.example.eventsourcing.repository.EventRepository;
import ch.example.eventsourcing.repository.EventSubscriptionRepository;
import ch.example.eventsourcing.service.AggregateFactory;
import ch.example.eventsourcing.service.AggregateStore;
import ch.example.eventsourcing.service.CommandProcessor;
import ch.example.eventsourcing.service.EventSubscriptionProcessor;
import ch.example.eventsourcing.service.PostgresChannelEventSubscriptionProcessor;
import ch.example.eventsourcing.service.ScheduledEventSubscriptionProcessor;
import ch.example.eventsourcing.service.command.CommandHandler;
import ch.example.eventsourcing.service.command.DefaultCommandHandler;
import ch.example.eventsourcing.service.event.AsyncEventHandler;
import ch.example.eventsourcing.service.event.SyncEventHandler;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(EventSourcingProperties.class)
@EnableAsync
public class EventSourcingAutoConfiguration {

    @Bean
    public AggregateRepository aggregateRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                                   ObjectMapper objectMapper,
                                                   AggregateTypeMapper aggregateTypeMapper) {
        return new AggregateRepository(jdbcTemplate, objectMapper, aggregateTypeMapper);
    }

    @Bean
    public EventRepository eventRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                           ObjectMapper objectMapper,
                                           EventTypeMapper eventTypeMapper) {
        return new EventRepository(jdbcTemplate, objectMapper, eventTypeMapper);
    }

    @Bean
    public EventSubscriptionRepository eventSubscriptionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new EventSubscriptionRepository(jdbcTemplate);
    }

    @Bean
    public AggregateFactory aggregateFactory(AggregateTypeMapper aggregateTypeMapper) {
        return new AggregateFactory(aggregateTypeMapper);
    }

    @Bean
    public AggregateStore aggregateStore(AggregateRepository aggregateRepository,
                                         EventRepository eventRepository,
                                         AggregateFactory aggregateFactory,
                                         EventSourcingProperties properties) {
        return new AggregateStore(aggregateRepository, eventRepository, aggregateFactory, properties);
    }

    @Bean
    public DefaultCommandHandler defaultCommandHandler() {
        return new DefaultCommandHandler();
    }

    @Bean
    public CommandProcessor commandProcessor(AggregateStore aggregateStore,
                                             List<CommandHandler<? extends Command>> commandHandlers,
                                             DefaultCommandHandler defaultCommandHandler,
                                             List<SyncEventHandler> syncEventHandlers) {
        return new CommandProcessor(aggregateStore, commandHandlers, defaultCommandHandler, syncEventHandlers);
    }

    @Bean
    public EventSubscriptionProcessor eventSubscriptionProcessor(EventSubscriptionRepository subscriptionRepository,
                                                                 EventRepository eventRepository) {
        return new EventSubscriptionProcessor(subscriptionRepository, eventRepository);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "event-sourcing.subscriptions", havingValue = "postgres-channel", matchIfMissing = true)
    static class PostgresChannelSubscriptionConfiguration {

        @Bean
        PostgresChannelEventSubscriptionProcessor postgresChannelEventSubscriptionProcessor(
                List<AsyncEventHandler> eventHandlers,
                EventSubscriptionProcessor eventSubscriptionProcessor,
                DataSourceProperties dataSourceProperties) {
            return new PostgresChannelEventSubscriptionProcessor(eventHandlers, eventSubscriptionProcessor, dataSourceProperties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(name = "event-sourcing.subscriptions", havingValue = "polling")
    static class PollingSubscriptionConfiguration {

        @Bean
        ScheduledEventSubscriptionProcessor scheduledEventSubscriptionProcessor(
                List<AsyncEventHandler> eventHandlers,
                EventSubscriptionProcessor eventSubscriptionProcessor) {
            return new ScheduledEventSubscriptionProcessor(eventHandlers, eventSubscriptionProcessor);
        }
    }
}
