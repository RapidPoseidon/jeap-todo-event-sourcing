package ch.example.todo.oauthmock;

import ch.admin.bit.jeap.oauth.mock.server.ServerApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Local stand-in for the federation's Keycloak: issues jEAP-shaped tokens so the stack can be run end to end.
 * The library's own application class is excluded from the scan because it has no visible constructor to proxy.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "ch.admin.bit.jeap.oauth.mock.server",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ServerApplication.class))
@EnableConfigurationProperties
public class TodoOauthMockServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoOauthMockServerApplication.class, args);
    }
}
