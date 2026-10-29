package com.vsdya.taskflow.auth.api;

import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
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

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUser() throws Exception {
        String email = "user-" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "StrongPassword123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        UserEntity savedUser = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo("StrongPassword123");
        assertThat(savedUser.getPasswordHash())
                .startsWith("$2");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateEmailAfterTrimmingAndCaseNormalization()
            throws Exception {
        String email = "duplicate-" + System.currentTimeMillis() + "@example.com";

        String firstRequestBody = """
                {
                    "email": "%s",
                    "password": "StrongPassword123"
                }
                """.formatted(email);

        String normalizedVariant = "  " + email.toUpperCase(Locale.ROOT) + " ";
        String secondRequestBody = """
                {
                    "email": "%s",
                    "password": "DifferentPassword123"
                }
                """.formatted(normalizedVariant);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value("User already exists: " + email));

        assertThat(userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .count()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidRegistration() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "not-an-email",
                                    "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectBlankRequiredRegistrationFields() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "   ",
                                    "password": "StrongPassword123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Email is required"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "blank-password@example.com",
                                    "password": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Password is required"));
    }

    @Test
    void shouldAcceptPasswordAtBcryptUtf8ByteLimit() throws Exception {
        String email = "password-boundary-" + System.currentTimeMillis()
                + "@example.com";
        String password = "🔐".repeat(18);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        assertThat(userRepository.findByEmailIgnoreCase(email)).isPresent();
    }

    @Test
    void shouldRejectRegistrationPasswordExceedingCharacterLimit()
            throws Exception {
        String password = "a".repeat(73);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "too-long-password@example.com",
                                    "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Password must be between 8 and 72 characters"));
    }

    @Test
    void shouldRejectRegistrationPasswordExceedingBcryptByteLimit()
            throws Exception {
        String password = "🔐".repeat(19);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "unicode-password@example.com",
                                    "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Password must not exceed 72 bytes when encoded as UTF-8"));
    }

    @Test
    void shouldRejectLoginPasswordExceedingBcryptByteLimit() throws Exception {
        String password = "🔐".repeat(19);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "unicode-password@example.com",
                                    "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Password must not exceed 72 bytes when encoded as UTF-8"));
    }
}
