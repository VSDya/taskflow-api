package com.vsdya.taskflow.project.application;

import com.vsdya.taskflow.project.api.CreateProjectRequest;
import com.vsdya.taskflow.project.domain.Project;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project create(CreateProjectRequest request) {
        var entity = new ProjectEntity(
                UUID.randomUUID(),
                request.name().trim(),
                request.description() == null ? null : request.description().trim(),
                Instant.now()
        );

        var saved = projectRepository.save(entity);
        return toDomain(saved);
    }

    private Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
