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
    public Project create(
            UUID ownerId,
            CreateProjectRequest request
    ) {
        var entity = new ProjectEntity(
                UUID.randomUUID(),
                ownerId,
                request.name().trim(),
                request.description() == null
                        ? null
                        : request.description().trim(),
                Instant.now()
        );

        return toDomain(projectRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<Project> findAll(
            UUID ownerId,
            Pageable pageable
    ) {
        return projectRepository
                .findAllByOwnerId(ownerId, pageable)
                .map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Project findById(
            UUID ownerId,
            UUID projectId
    ) {
        return toDomain(getEntity(ownerId, projectId));
    }

    @Transactional
    public Project update(
            UUID ownerId,
            UUID projectId,
            UpdateProjectRequest request
    ) {
        var entity = getEntity(ownerId, projectId);

        entity.update(
                request.name().trim(),
                request.description() == null
                        ? null
                        : request.description().trim()
        );

        return toDomain(entity);
    }

    @Transactional
    public void delete(
            UUID ownerId,
            UUID projectId
    ) {
        var entity = getEntity(ownerId, projectId);
        projectRepository.delete(entity);
    }

    private ProjectEntity getEntity(
            UUID ownerId,
            UUID projectId
    ) {
        return projectRepository
                .findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
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