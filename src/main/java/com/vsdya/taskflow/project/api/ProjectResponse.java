package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.domain.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
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
