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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldReturnProjectById() {
        var id = UUID.randomUUID();
        var entity = new ProjectEntity(id, "TaskFlow", "Portfolio API", Instant.now());
        when(projectRepository.findById(id)).thenReturn(java.util.Optional.of(entity));

        var project = projectService.findById(id);

        assertThat(project.id()).isEqualTo(id);
        assertThat(project.name()).isEqualTo("TaskFlow");
    }

    @Test
    void shouldThrowWhenProjectDoesNotExist() {
        var id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> projectService.findById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found: " + id);
    }
}
