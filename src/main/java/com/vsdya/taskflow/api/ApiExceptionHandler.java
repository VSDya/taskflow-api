package com.vsdya.taskflow.api;

import com.vsdya.taskflow.auth.application.InvalidCredentialsException;
import com.vsdya.taskflow.auth.application.UserAlreadyExistsException;
import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.task.application.TaskNotFoundException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleProjectNotFound(
            ProjectNotFoundException exception
    ) {
        return new ErrorResponse(
                404,
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFound(
            TaskNotFoundException exception
    ) {
        return new ErrorResponse(
                404,
                "TASK_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExists(
            UserAlreadyExistsException exception
    ) {
        return new ErrorResponse(
                409,
                "USER_ALREADY_EXISTS",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return new ErrorResponse(
                401,
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        return new ErrorResponse(
                400,
                "VALIDATION_ERROR",
                message,
                Instant.now()
        );
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidSortProperty(
            PropertyReferenceException exception
    ) {
        return new ErrorResponse(
                400,
                "INVALID_SORT_PROPERTY",
                "Invalid sort property: " + exception.getPropertyName(),
                Instant.now()
        );
    }

    @Schema(
            name = "ErrorResponse",
            description = "Standard JSON error response returned by the API"
    )
    public record ErrorResponse(

            @Schema(
                    description = "HTTP status code",
                    example = "401",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            int status,

            @Schema(
                    description = "Machine-readable error code",
                    example = "UNAUTHORIZED",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String error,

            @Schema(
                    description = "Human-readable explanation of the error",
                    example = "Authentication is required or the token is invalid",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String message,

            @Schema(
                    description = "Timestamp when the error occurred, in UTC",
                    example = "2026-10-26T15:00:00Z",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Instant timestamp
    ) {
    }
}