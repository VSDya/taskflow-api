package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.task.domain.Task;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(
        name = "TaskResponse",
        description = "Task returned by the TaskFlow API"
)
public record TaskResponse(

        @Schema(
                description = "Unique task identifier",
                example = "bab411f1-a88b-4fc9-ac35-2e55cadf742b",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID id,

        @Schema(
                description = "Identifier of the project to which this task belongs",
                example = "ce32898f-6178-4cdf-923e-9c0b922ae2b4",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID projectId,

        @Schema(
                description = "Short task title",
                example = "Implement user authentication",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        @Schema(
                description = "Optional detailed task description",
                example = "Implement and test JWT authentication",
                nullable = true
        )
        String description,

        @Schema(
                description = "Current task status",
                example = "TODO",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        TaskStatus status,

        @Schema(
                description = "Task priority",
                example = "MEDIUM",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        TaskPriority priority,

        @Schema(
                description = "Optional due date in ISO format (yyyy-MM-dd)",
                example = "2026-11-15",
                nullable = true
        )
        LocalDate dueDate,

        @Schema(
                description = "Timestamp when the task was created, in UTC",
                example = "2026-10-26T12:54:26Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant createdAt,

        @Schema(
                description = "Timestamp of the most recent task update, in UTC",
                example = "2026-10-26T12:54:26Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.id(),
                task.projectId(),
                task.title(),
                task.description(),
                task.status(),
                task.priority(),
                task.dueDate(),
                task.createdAt(),
                task.updatedAt()
        );
    }
}