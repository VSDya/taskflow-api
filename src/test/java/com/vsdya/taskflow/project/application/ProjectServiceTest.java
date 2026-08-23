package com.vsdya.taskflow.project.application;

import com.vsdya.taskflow.project.api.CreateProjectRequest;
import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldReturnProjectForOwner() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        var entity = new ProjectEntity(
                projectId,
                ownerId,
                "TaskFlow",
                "Portfolio API",
                Instant.now()
        );

        when(projectRepository.findByIdAndOwnerId(projectId, ownerId))
                .thenReturn(Optional.of(entity));

        var project = projectService.findById(ownerId, projectId);

        assertThat(project.id()).isEqualTo(projectId);
        assertThat(project.name()).isEqualTo("TaskFlow");
    }

    @Test
    void shouldThrowWhenProjectDoesNotBelongToOwner() {
        var ownerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectRepository.findByIdAndOwnerId(projectId, ownerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.findById(ownerId, projectId)
        )
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found: " + projectId);
    }

    @Test
    void shouldCreateProjectForOwner() {
        var ownerId = UUID.randomUUID();

        when(projectRepository.save(any(ProjectEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var project = projectService.create(
                ownerId,
                new CreateProjectRequest(
                        " TaskFlow ",
                        " Portfolio API "
                )
        );

        assertThat(project.name()).isEqualTo("TaskFlow");
        assertThat(project.description()).isEqualTo("Portfolio API");

        var captor = org.mockito.ArgumentCaptor
                .forClass(ProjectEntity.class);

        verify(projectRepository).save(captor.capture());

        assertThat(captor.getValue().getOwnerId())
                .isEqualTo(ownerId);
    }
}