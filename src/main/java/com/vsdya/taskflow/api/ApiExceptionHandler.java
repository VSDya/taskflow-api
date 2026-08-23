package com.vsdya.taskflow.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.task.application.TaskNotFoundException;
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
    public ErrorResponse handleProjectNotFound(ProjectNotFoundException exception) {
        return new ErrorResponse(
                404,
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFound(TaskNotFoundException exception) {
        return new ErrorResponse(
                404,
                "TASK_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
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

    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {
    }
}