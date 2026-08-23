package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.security.JwtService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectOwnershipIntegrationTest {

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
    private JwtService jwtService;

    @Test
    void shouldAllowOwnerToReadOwnProject() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID projectId = createProject(ownerId);

        mockMvc.perform(
                get("/api/v1/projects/{id}", projectId)
                        .header("Authorization", bearerToken(ownerId))
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectOtherUserFromReadingProject() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");
        UUID projectId = createProject(ownerId);

        mockMvc.perform(
                get("/api/v1/projects/{id}", projectId)
                        .header("Authorization", bearerToken(otherUserId))
        ).andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectOtherUserFromUpdatingProject() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");
        UUID projectId = createProject(ownerId);

        mockMvc.perform(
                put("/api/v1/projects/{id}", projectId)
                        .header("Authorization", bearerToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Hacked project",
                                    "description": "Should not be allowed"
                                }
                                """)
        ).andExpect(status().isNotFound());

        var stored = projectRepository.findById(projectId).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(stored.getName())
                .isEqualTo("Owned project");
    }

    @Test
    void shouldRejectOtherUserFromDeletingProject() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");
        UUID projectId = createProject(ownerId);

        mockMvc.perform(
                delete("/api/v1/projects/{id}", projectId)
                        .header("Authorization", bearerToken(otherUserId))
        ).andExpect(status().isNotFound());

        org.assertj.core.api.Assertions.assertThat(
                projectRepository.findById(projectId)
        ).isPresent();
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
                        "Project owned by test user",
                        Instant.now()
                )
        );

        return projectId;
    }

    private String bearerToken(UUID userId) {
        return "Bearer " + jwtService.generateToken(
                userId,
                "user@example.com"
        );
    }
}