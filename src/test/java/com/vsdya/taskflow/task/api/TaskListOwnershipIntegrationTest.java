package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.security.JwtService;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;
import com.vsdya.taskflow.task.infrastructure.TaskEntity;
import com.vsdya.taskflow.task.infrastructure.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskListOwnershipIntegrationTest {

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
    void shouldRejectOtherUserFromListingTasksOfForeignProject() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");

        UUID projectId = createProject(ownerId);
        createTask(projectId);

        mockMvc.perform(
                get("/api/v1/projects/{projectId}/tasks", projectId)
                        .header("Authorization", bearerToken(otherUserId))
        ).andExpect(status().isNotFound());
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
                        "Task list ownership test",
                        Instant.now()
                )
        );

        return projectId;
    }

    private void createTask(UUID projectId) {
        Instant now = Instant.now();

        taskRepository.save(
                new TaskEntity(
                        UUID.randomUUID(),
                        projectId,
                        "Private task",
                        "Should not be visible",
                        TaskStatus.TODO,
                        TaskPriority.MEDIUM,
                        null,
                        now,
                        now
                )
        );
    }

    private String bearerToken(UUID userId) {
        return "Bearer " + jwtService.generateToken(
                userId,
                "user@example.com"
        );
    }
}