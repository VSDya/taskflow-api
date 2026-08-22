package com.vsdya.taskflow.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.task.application.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleProjectNotFound(ProjectNotFoundException exception) {
        return new ErrorResponse("PROJECT_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFound(TaskNotFoundException exception) {
        return new ErrorResponse("TASK_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    public record ErrorResponse(String code, String message, Instant timestamp) {
    }
}
