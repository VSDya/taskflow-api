package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.task.application.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return TaskResponse.from(
                taskService.create(userId, projectId, request)
        );
    }

    @GetMapping("/projects/{projectId}/tasks")
    public Page<TaskResponse> findByProject(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return taskService
                .findByProject(userId, projectId, pageable)
                .map(TaskResponse::from);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse findById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return TaskResponse.from(
                taskService.findById(userId, id)
        );
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return TaskResponse.from(
                taskService.update(userId, id, request)
        );
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        taskService.delete(userId, id);
    }
}