package com.vsdya.taskflow.project.application;

import com.vsdya.taskflow.project.api.CreateProjectRequest;
import com.vsdya.taskflow.project.domain.Project;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        return toDomain(projectRepository.save(entity));
    }

    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::toDomain);
    }

    public Project findById(UUID id) {
        return projectRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ProjectNotFoundException(id));
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
