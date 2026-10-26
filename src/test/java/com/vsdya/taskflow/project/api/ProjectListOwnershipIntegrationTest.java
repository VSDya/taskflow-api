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
    void shouldReturnOnlyProjectsOwnedByAuthenticatedUser()
            throws Exception {

        UUID ownerId = createUser(
                "owner-" + UUID.randomUUID() + "@example.com"
        );
        UUID otherUserId = createUser(
                "other-" + UUID.randomUUID() + "@example.com"
        );

        createProject(ownerId, "Owner project");
        createProject(otherUserId, "Other user's project");

        mockMvc.perform(
                        get("/api/v1/projects")
                                .header(
                                        "Authorization",
                                        bearerToken(ownerId)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Owner project"));
    }

    @Test
    void shouldUseDefaultPageSizeOfTwenty() throws Exception {
        UUID ownerId = createUser(
                "pagination-" + UUID.randomUUID() + "@example.com"
        );

        createProject(ownerId, "Pagination project");

        mockMvc.perform(
                        get("/api/v1/projects")
                                .header(
                                        "Authorization",
                                        bearerToken(ownerId)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldLimitRequestedPageSizeToOneHundred() throws Exception {
        UUID ownerId = createUser(
                "pagination-" + UUID.randomUUID() + "@example.com"
        );

        createProject(ownerId, "Pagination project");

        mockMvc.perform(
                        get("/api/v1/projects")
                                .param("page", "0")
                                .param("size", "500")
                                .header(
                                        "Authorization",
                                        bearerToken(ownerId)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void shouldReturnStructured400ForUnknownSortProperty()
            throws Exception {

        UUID ownerId = createUser(
                "sorting-" + UUID.randomUUID() + "@example.com"
        );

        createProject(ownerId, "Sorting test project");

        mockMvc.perform(
                        get("/api/v1/projects")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sort", "unknownField,asc")
                                .header(
                                        "Authorization",
                                        bearerToken(ownerId)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("INVALID_SORT_PROPERTY"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sort property: unknownField"))
                .andExpect(jsonPath("$.timestamp").exists());
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
                        "Ownership and pagination test",
                        Instant.now()
                )
        );
    }

    private String bearerToken(UUID userId) {
        return "Bearer " + jwtService.generateToken(
                userId,
                "owner@example.com"
        );
    }
}