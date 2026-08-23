package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.security.JwtService;
import com.vsdya.taskflow.task.infrastructure.TaskEntity;
import com.vsdya.taskflow.task.infrastructure.TaskRepository;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskOwnershipIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldAllowOwnerToReadOwnTask() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID projectId = createProject(ownerId);
        UUID taskId = createTask(projectId);

        mockMvc.perform(
                get("/api/v1/tasks/{id}", taskId)
                        .header("Authorization", bearerToken(ownerId))
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectOtherUserFromReadingTask() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");

        UUID projectId = createProject(ownerId);
        UUID taskId = createTask(projectId);

        mockMvc.perform(
                get("/api/v1/tasks/{id}", taskId)
                        .header("Authorization", bearerToken(otherUserId))
        ).andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectOtherUserFromUpdatingTask() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");

        UUID projectId = createProject(ownerId);
        UUID taskId = createTask(projectId);

        mockMvc.perform(
                put("/api/v1/tasks/{id}", taskId)
                        .header("Authorization", bearerToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Hacked task",
                                    "description": "Should not be allowed",
                                    "status": "DONE",
                                    "priority": "HIGH",
                                    "dueDate": null
                                }
                                """)
        ).andExpect(status().isNotFound());

        var stored = taskRepository.findById(taskId).orElseThrow();

        assertThat(stored.getTitle())
                .isEqualTo("Owned task");
        assertThat(stored.getStatus())
                .isEqualTo(TaskStatus.TODO);
    }

    @Test
    void shouldRejectOtherUserFromDeletingTask() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");

        UUID projectId = createProject(ownerId);
        UUID taskId = createTask(projectId);

        mockMvc.perform(
                delete("/api/v1/tasks/{id}", taskId)
                        .header("Authorization", bearerToken(otherUserId))
        ).andExpect(status().isNotFound());

        assertThat(taskRepository.findById(taskId))
                .isPresent();
    }

    private UUID createUser(String email) {
        UUID userId = UUID.randomUUID();

        userRepository.save(
                new UserEntity(
                        userId,
                        email,
                        "hashed-password",
                        Instant.now()
                )
        );

        return userId;
    }

    private UUID createProject(UUID ownerId) {
        UUID projectId = UUID.randomUUID();

        projectRepository.save(
                new ProjectEntity(
                        projectId,
                        ownerId,
                        "Owned project",
                        "Project for task ownership test",
                        Instant.now()
                )
        );

        return projectId;
    }

    private UUID createTask(UUID projectId) {
        UUID taskId = UUID.randomUUID();
        Instant now = Instant.now();

        taskRepository.save(
                new TaskEntity(
                        taskId,
                        projectId,
                        "Owned task",
                        "Task owned through project",
                        TaskStatus.TODO,
                        TaskPriority.MEDIUM,
                        null,
                        now,
                        now
                )
        );

        return taskId;
    }

    private String bearerToken(UUID userId) {
        return "Bearer " + jwtService.generateToken(
                userId,
                "user@example.com"
        );
    }
}