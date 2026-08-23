package com.vsdya.taskflow.auth.application;

import com.vsdya.taskflow.auth.api.AuthResponse;
import com.vsdya.taskflow.auth.api.LoginRequest;
import com.vsdya.taskflow.auth.api.RegisterRequest;
import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
import com.vsdya.taskflow.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

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

        return new AuthResponse(
                "User registered successfully",
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        return new AuthResponse(
                "Login successful",
                token,
                "Bearer"
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}