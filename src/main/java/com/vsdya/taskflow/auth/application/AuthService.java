package com.vsdya.taskflow.auth.application;

import com.vsdya.taskflow.auth.api.AuthResponse;
import com.vsdya.taskflow.auth.api.RegisterRequest;
import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                email,
                passwordHash,
                Instant.now()
        );

        userRepository.save(user);

        return new AuthResponse("User registered successfully");
    }
}