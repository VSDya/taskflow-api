package com.vsdya.taskflow.task.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    Page<TaskEntity> findByProjectId(UUID projectId, Pageable pageable);
    boolean existsByProjectId(UUID projectId);
}
