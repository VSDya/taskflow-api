package com.vsdya.taskflow.auth.api;

public record AuthResponse(
        String message,
        String token,
        String tokenType
) {
}