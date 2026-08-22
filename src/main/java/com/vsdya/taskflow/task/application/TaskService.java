package com.vsdya.taskflow.task.application;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.task.api.CreateTaskRequest;
import com.vsdya.taskflow.task.api.UpdateTaskRequest;
import com.vsdya.taskflow.task.domain.Task;
import com.vsdya.taskflow.task.domain.TaskStatus;
import com.vsdya.taskflow.task.infrastructure.TaskEntity;
import com.vsdya.taskflow.task.infrastructure.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Task create(UUID projectId, CreateTaskRequest request) {
        ensureProjectExists(projectId);
        var now = Instant.now();
        var entity = new TaskEntity(
                UUID.randomUUID(), projectId, request.title().trim(),
                request.description() == null ? null : request.description().trim(),
                TaskStatus.TODO, request.priority(), request.dueDate(), now, now
        );
        return toDomain(taskRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<Task> findByProject(UUID projectId, Pageable pageable) {
        ensureProjectExists(projectId);
        return taskRepository.findByProjectId(projectId, pageable).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task update(UUID id, UpdateTaskRequest request) {
        var entity = getEntity(id);
        entity.update(
                request.title().trim(),
                request.description() == null ? null : request.description().trim(),
                request.status(), request.priority(), request.dueDate()
        );
        return toDomain(entity);
    }

    @Transactional
    public void delete(UUID id) {
        taskRepository.delete(getEntity(id));
    }

    private TaskEntity getEntity(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void ensureProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    private Task toDomain(TaskEntity entity) {
        return new Task(entity.getId(), entity.getProjectId(), entity.getTitle(),
                entity.getDescription(), entity.getStatus(), entity.getPriority(),
                entity.getDueDate(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
