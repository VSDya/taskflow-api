package com.vsdya.taskflow.task.application;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.task.api.CreateTaskRequest;
import com.vsdya.taskflow.task.infrastructure.TaskEntity;
import com.vsdya.taskflow.task.infrastructure.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void shouldCreateTaskForProjectOwnedByUser() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(true);

        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        var task = taskService.create(
                ownerId,
                projectId,
                new CreateTaskRequest(
                        "Write tests",
                        "Add task ownership tests",
                        null,
                        null
                )
        );

        assertThat(task.projectId()).isEqualTo(projectId);
        assertThat(task.title()).isEqualTo("Write tests");

        ArgumentCaptor<TaskEntity> captor =
                ArgumentCaptor.forClass(TaskEntity.class);

        verify(taskRepository).save(captor.capture());

        assertThat(captor.getValue().getProjectId())
                .isEqualTo(projectId);
    }

    @Test
    void shouldRejectTaskCreationForForeignProject() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(false);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        assertThatThrownBy(() ->
                taskService.create(
                        ownerId,
                        projectId,
                        new CreateTaskRequest(
                                "Write tests",
                                "Should not be created",
                                null,
                                null
                        )
                )
        )
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void shouldRejectReadingTaskFromForeignProject() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(
                        new TaskEntity(
                                taskId,
                                projectId,
                                "Private task",
                                "Foreign project task",
                                com.vsdya.taskflow.task.domain.TaskStatus.TODO,
                                com.vsdya.taskflow.task.domain.TaskPriority.MEDIUM,
                                null,
                                Instant.now(),
                                Instant.now()
                        )
                ));

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(false);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        assertThatThrownBy(() ->
                taskService.findById(ownerId, taskId)
        )
                .isInstanceOf(ProjectNotFoundException.class);
    }
}