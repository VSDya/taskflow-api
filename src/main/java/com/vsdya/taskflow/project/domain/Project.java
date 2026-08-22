package com.vsdya.taskflow.project.domain;

import java.time.Instant;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
}
