package com.vsdya.taskflow.task.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    Page<TaskEntity> findByProjectId(UUID projectId, Pageable pageable);

    Optional<TaskEntity> findByIdAndProjectId(UUID id, UUID projectId);

    boolean existsByProjectId(UUID projectId);
}