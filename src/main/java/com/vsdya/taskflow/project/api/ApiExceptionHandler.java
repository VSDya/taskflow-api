package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ApiError handleProjectNotFound(ProjectNotFoundException exception) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
        );
    }
}
