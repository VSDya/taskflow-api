package com.vsdya.taskflow.auth.application;

import com.vsdya.taskflow.auth.api.AuthResponse;
import com.vsdya.taskflow.auth.api.RegisterRequest;
import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository =
            org.mockito.Mockito.mock(UserRepository.class);

    private final PasswordEncoder passwordEncoder =
            org.mockito.Mockito.mock(PasswordEncoder.class);

    private final AuthService authService =
            new AuthService(userRepository, passwordEncoder);

    @Test
    void shouldRegisterUser() {
        var request = new RegisterRequest(
                " TEST@example.com ",
                "StrongPassword123"
        );

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("StrongPassword123"))
                .thenReturn("hashed-password");

        AuthResponse response = authService.register(request);

        assertThat(response.message())
                .isEqualTo("User registered successfully");

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("test@example.com");

        assertThat(savedUser.getPasswordHash())
                .isEqualTo("hashed-password");
    }

    @Test
    void shouldRejectExistingUser() {
        var request = new RegisterRequest(
                "test@example.com",
                "StrongPassword123"
        );

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
