package com.vsdya.taskflow.auth.application;

import com.vsdya.taskflow.auth.api.AuthResponse;
import com.vsdya.taskflow.auth.api.LoginRequest;
import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import com.vsdya.taskflow.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceLoginTest {

    private final UserRepository userRepository =
            mock(UserRepository.class);

    private final PasswordEncoder passwordEncoder =
            mock(PasswordEncoder.class);

    private final JwtService jwtService =
            mock(JwtService.class);

    private final AuthService authService =
            new AuthService(
                    userRepository,
                    passwordEncoder,
                    jwtService
            );

    @Test
    void shouldLoginUserAndReturnJwt() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity(
                userId,
                "test@example.com",
                "hashed-password",
                Instant.now()
        );

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "StrongPassword123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                userId,
                "test@example.com"
        )).thenReturn("jwt-token");

        AuthResponse response = authService.login(
                new LoginRequest(
                        " TEST@example.com ",
                        "StrongPassword123"
                )
        );

        assertThat(response.message())
                .isEqualTo("Login successful");

        assertThat(response.token())
                .isEqualTo("jwt-token");

        assertThat(response.tokenType())
                .isEqualTo("Bearer");
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                authService.login(
                        new LoginRequest(
                                "unknown@example.com",
                                "StrongPassword123"
                        )
                )
        ).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRejectWrongPassword() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity(
                userId,
                "test@example.com",
                "hashed-password",
                Instant.now()
        );

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(
                        new LoginRequest(
                                "test@example.com",
                                "WrongPassword"
                        )
                )
        ).isInstanceOf(InvalidCredentialsException.class);
    }
}