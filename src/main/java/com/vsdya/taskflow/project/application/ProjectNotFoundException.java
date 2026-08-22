package com.vsdya.taskflow.project.application;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(UUID id) {
        super("Project not found: " + id);
    }
}
