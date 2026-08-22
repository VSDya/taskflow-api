package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.task.domain.Task;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.id(), task.projectId(), task.title(), task.description(),
                task.status(), task.priority(), task.dueDate(),
                task.createdAt(), task.updatedAt()
        );
    }
}
