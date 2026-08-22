package com.vsdya.taskflow.task.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Task(
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
}
