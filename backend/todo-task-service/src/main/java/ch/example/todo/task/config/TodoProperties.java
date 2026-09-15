package ch.example.todo.task.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "todo")
public class TodoProperties {

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {

        private List<String> allowedOrigins = List.of("http://localhost:4200");
    }
}
