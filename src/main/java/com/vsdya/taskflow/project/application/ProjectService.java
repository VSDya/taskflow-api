package com.vsdya.taskflow.project.application;

import com.vsdya.taskflow.project.api.CreateProjectRequest;
import com.vsdya.taskflow.project.domain.Project;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProjectService {

    public Project create(CreateProjectRequest request) {
        return new Project(
                UUID.randomUUID(),
                request.name().trim(),
                request.description() == null ? null : request.description().trim(),
                Instant.now()
        );
    }
}
