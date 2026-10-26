package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.domain.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        name = "ProjectResponse",
        description = "Project returned by the TaskFlow API"
)
public record ProjectResponse(

        @Schema(
                description = "Unique project identifier",
                example = "ce32898f-6178-4cdf-923e-9c0b922ae2b4",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID id,

        @Schema(
                description = "Project name",
                example = "My First TaskFlow Project",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Schema(
                description = "Optional project description",
                example = "A project for organizing tasks",
                nullable = true
        )
        String description,

        @Schema(
                description = "Timestamp when the project was created, in UTC",
                example = "2026-10-26T12:47:57Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.id(),
                project.name(),
                project.description(),
                project.createdAt()
        );
    }
}