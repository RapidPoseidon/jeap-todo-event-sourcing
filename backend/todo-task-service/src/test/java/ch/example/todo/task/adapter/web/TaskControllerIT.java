package ch.example.todo.task.adapter.web;

import ch.admin.bit.jeap.security.test.WithJeapAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the command side, the synchronous read model and the asynchronous subscription against a real PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class TaskControllerIT {

    private static final String READ_ROLE = "todo_@task_#read";
    private static final String WRITE_ROLE = "todo_@task_#write";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TaskControllerIT::jdbcUrlWithDataSchema);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static String jdbcUrlWithDataSchema() {
        String jdbcUrl = POSTGRES.getJdbcUrl();
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "currentSchema=data";
    }

    @Test
    @WithJeapAuthenticationToken(userRoles = {READ_ROLE, WRITE_ROLE}, username = "anna")
    void taskLifecycle_isRecordedAsEventsAndProjected() throws Exception {
        String taskId = createTask();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Write the RFC (v2)", "priority": "MEDIUM"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED"}
                                """))
                .andExpect(status().isOk());

        JsonNode history = readJson(get("/api/tasks/" + taskId + "/events"));
        assertThat(history.size()).isEqualTo(3);
        assertThat(history.get(0).get("eventType").asString()).isEqualTo("TODO_TASK_CREATED");
        assertThat(history.get(2).get("eventType").asString()).isEqualTo("TODO_TASK_COMPLETED");
        assertThat(history.get(0).get("initiatedBy").asString()).isEqualTo("anna");

        JsonNode atVersionOne = readJson(get("/api/tasks/" + taskId + "/versions/1"));
        assertThat(atVersionOne.get("title").asString()).isEqualTo("Write the RFC");
        assertThat(atVersionOne.get("status").asString()).isEqualTo("OPEN");

        await().pollInSameThread().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            JsonNode statistics = readJson(get("/api/statistics"));
            assertThat(statistics.get("createdCount").asLong()).isPositive();
            assertThat(statistics.get("completedCount").asLong()).isPositive();
        });
    }

    @Test
    @WithJeapAuthenticationToken(userRoles = {READ_ROLE, WRITE_ROLE}, username = "anna")
    void completeTask_twice_isRejectedWithUnprocessableContent() throws Exception {
        String taskId = createTask();
        String body = """
                {"status": "COMPLETED"}
                """;

        mockMvc.perform(put("/api/tasks/" + taskId + "/status").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/tasks/" + taskId + "/status").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithJeapAuthenticationToken(userRoles = READ_ROLE, username = "ben")
    void createTask_withoutWriteRole_isForbidden() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Not allowed"}
                                """))
                .andExpect(status().isForbidden());
    }

    private String createTask() throws Exception {
        String created = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Write the RFC", "description": "Draft it", "priority": "HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(created).get("id").asString();
    }

    private JsonNode readJson(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        return objectMapper.readTree(mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
