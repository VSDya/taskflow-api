package com.vsdya.taskflow.auth.api;

import com.vsdya.taskflow.api.ApiExceptionHandler;
import com.vsdya.taskflow.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "User registration and JWT authentication"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account.
                    Email must be valid.
                    Password must contain between 8 and 72 characters and no more than 72 UTF-8 bytes.
                    Registration does not return a JWT.
                    Use the login endpoint to obtain an access token.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with this email already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in and obtain a JWT",
            description = """
                    Authenticates a user using email and password.
                    Returns a signed JWT access token.
                    Use the token with the Authorize button to access
                    protected projects and tasks endpoints.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful; JWT returned"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}