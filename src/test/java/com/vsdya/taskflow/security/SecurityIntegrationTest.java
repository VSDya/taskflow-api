package com.vsdya.taskflow.security;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String TEST_JWT_SECRET =
            "test-only-signing-key-do-not-use-outside-tests-0123456789abcdef";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectProtectedEndpointWithoutTokenUsingStructuredError()
            throws Exception {
        mockMvc.perform(
                get("/api/v1/projects")
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRequireAuthenticationForUnlistedAuthRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "test@example.com"
        );

        mockMvc.perform(
                get("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectMalformedToken() throws Exception {
        mockMvc.perform(
                get("/api/v1/projects")
                        .header("Authorization", "Bearer not-a-jwt")
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRejectTokenWithTamperedSignature() throws Exception {
        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "test@example.com"
        );
        String[] tokenParts = token.split("\\.");
        char firstSignatureCharacter = tokenParts[2].charAt(0);
        char replacement = firstSignatureCharacter == 'A' ? 'B' : 'A';
        tokenParts[2] = replacement + tokenParts[2].substring(1);
        String tamperedToken = String.join(".", tokenParts);

        mockMvc.perform(
                get("/api/v1/projects")
                        .header("Authorization", "Bearer " + tamperedToken)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        JwtService expiredTokenIssuer = new JwtService(TEST_JWT_SECRET, -60_000);
        String expiredToken = expiredTokenIssuer.generateToken(
                UUID.randomUUID(),
                "expired@example.com"
        );

        mockMvc.perform(
                get("/api/v1/projects")
                        .header("Authorization", "Bearer " + expiredToken)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
