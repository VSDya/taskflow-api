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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectListOwnershipIntegrationTest {

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
    void shouldReturnOnlyProjectsOwnedByAuthenticatedUser() throws Exception {
        UUID ownerId = createUser("owner-" + UUID.randomUUID() + "@example.com");
        UUID otherUserId = createUser("other-" + UUID.randomUUID() + "@example.com");

        createProject(ownerId, "Owner project");
        createProject(otherUserId, "Other user's project");

        String token = "Bearer " + jwtService.generateToken(
                ownerId,
                "owner@example.com"
        );

        mockMvc.perform(
                get("/api/v1/projects")
                        .header("Authorization", token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Owner project"));
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

    private void createProject(UUID ownerId, String name) {
        projectRepository.save(
                new ProjectEntity(
                        UUID.randomUUID(),
                        ownerId,
                        name,
                        "Ownership test",
                        Instant.now()
                )
        );
    }
}