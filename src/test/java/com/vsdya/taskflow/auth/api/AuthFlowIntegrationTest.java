package com.vsdya.taskflow.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterLoginAndAccessProtectedEndpoint() throws Exception {
        String email = "flow-" + System.currentTimeMillis() + "@example.com";
        String password = "StrongPassword123";

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        assertThat(userRepository.findByEmailIgnoreCase(email))
                .isPresent();

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Login successful"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.token")
                        .isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(loginResponse);
        String token = response.get("token").asText();

        mockMvc.perform(
                        get("/api/v1/projects")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }
}