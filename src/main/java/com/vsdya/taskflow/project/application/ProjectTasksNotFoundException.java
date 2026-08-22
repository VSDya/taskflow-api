package com.vsdya.taskflow.project.application;

import java.util.UUID;

public class ProjectTasksNotFoundException extends RuntimeException {
    public ProjectTasksNotFoundException(UUID projectId) {
        super("Project not found: " + projectId);
    }
}
