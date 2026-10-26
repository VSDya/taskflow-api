package com.vsdya.taskflow.api;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiErrorIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnStructured400ForInvalidRegistration()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "not-an-email",
                                            "password": "short"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnStructured401ForInvalidCredentials()
            throws Exception {

        String email =
                "missing-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "StrongPassword123"
                                        }
                                        """.formatted(email))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnStructured404ForMissingProject()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        String token = jwtService.generateToken(
                userId,
                "api-error-test@example.com"
        );

        mockMvc.perform(
                        get("/api/v1/projects/{id}", projectId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Project not found: " + projectId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnStructured409ForDuplicateEmail()
            throws Exception {

        String email =
                "duplicate-" + UUID.randomUUID() + "@example.com";

        String registerRequest = """
                {
                    "email": "%s",
                    "password": "StrongPassword123"
                }
                """.formatted(email);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value("User already exists: " + email))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}