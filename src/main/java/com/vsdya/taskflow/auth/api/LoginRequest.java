package com.vsdya.taskflow.auth.api;

import com.vsdya.taskflow.auth.api.validation.PasswordByteLength;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "LoginRequest",
        description = "Credentials used to authenticate a TaskFlow user"
)
public record LoginRequest(

        @Schema(
                description = "Email address used during registration",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(
                description = "User password. Must not exceed 72 UTF-8 bytes.",
                example = "StrongPassword123",
                format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password is required")
        @PasswordByteLength
        String password
) {
}