package com.vsdya.taskflow.project.application;

import com.vsdya.taskflow.project.api.CreateProjectRequest;
import com.vsdya.taskflow.project.api.UpdateProjectRequest;
import com.vsdya.taskflow.project.domain.Project;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Project create(CreateProjectRequest request) {
        var entity = new ProjectEntity(
                UUID.randomUUID(),
                request.name().trim(),
                request.description() == null ? null : request.description().trim(),
                Instant.now()
        );

        return toDomain(projectRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Project findById(UUID id) {
        return toDomain(getEntity(id));
    }

    @Transactional
    public Project update(UUID id, UpdateProjectRequest request) {
        var entity = getEntity(id);
        entity.update(
                request.name().trim(),
                request.description() == null ? null : request.description().trim()
        );
        return toDomain(entity);
    }

    @Transactional
    public void delete(UUID id) {
        var entity = getEntity(id);
        projectRepository.delete(entity);
    }

    private ProjectEntity getEntity(UUID id) {
        return projectRepository.findById(id)
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
