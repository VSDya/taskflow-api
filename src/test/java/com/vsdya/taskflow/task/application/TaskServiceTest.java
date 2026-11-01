
package com.vsdya.taskflow.task.application;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import com.vsdya.taskflow.task.api.CreateTaskRequest;
import com.vsdya.taskflow.task.api.UpdateTaskRequest;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;
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
import static org.mockito.Mockito.never;
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
                                TaskStatus.TODO,
                                TaskPriority.MEDIUM,
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
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void shouldCreateTaskWithNullDescription() {
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
                        null,
                        null,
                        null
                )
        );

        assertThat(task.title()).isEqualTo("Write tests");
        assertThat(task.description()).isNull();
    }

    @Test
    void shouldUpdateTaskWithDescription() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();

        var entity = new TaskEntity(
                taskId,
                projectId,
                "Old title",
                "Old description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                Instant.now(),
                Instant.now()
        );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(entity));

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(true);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        var task = taskService.update(
                ownerId,
                taskId,
                new UpdateTaskRequest(
                        " New title ",
                        " New description ",
                        TaskStatus.IN_PROGRESS,
                        TaskPriority.HIGH,
                        null
                )
        );

        assertThat(task.title()).isEqualTo("New title");
        assertThat(task.description()).isEqualTo("New description");
    }

    @Test
    void shouldUpdateTaskWithNullDescription() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();

        var entity = new TaskEntity(
                taskId,
                projectId,
                "Old title",
                "Old description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                Instant.now(),
                Instant.now()
        );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(entity));

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(true);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        var task = taskService.update(
                ownerId,
                taskId,
                new UpdateTaskRequest(
                        "New title",
                        null,
                        TaskStatus.TODO,
                        TaskPriority.MEDIUM,
                        null
                )
        );

        assertThat(task.title()).isEqualTo("New title");
        assertThat(task.description()).isNull();
    }

    @Test
    void shouldDeleteTaskForProjectOwnedByUser() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();

        var entity = new TaskEntity(
                taskId,
                projectId,
                "Task to delete",
                "Description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                Instant.now(),
                Instant.now()
        );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(entity));

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(true);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        taskService.delete(ownerId, taskId);

        verify(taskRepository).delete(entity);
    }

    @Test
    void shouldRejectDeletingTaskFromForeignProject() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();

        var entity = new TaskEntity(
                taskId,
                projectId,
                "Private task",
                "Description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                Instant.now(),
                Instant.now()
        );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(entity));

        when(projectRepository.existsByIdAndOwnerId(projectId, ownerId))
                .thenReturn(false);

        var taskService = new TaskService(
                taskRepository,
                projectRepository
        );

        assertThatThrownBy(() ->
                taskService.delete(ownerId, taskId)
        )
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).delete(any(TaskEntity.class));
    }
}
