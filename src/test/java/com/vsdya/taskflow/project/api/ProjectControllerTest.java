package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.application.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void shouldCreateProject() throws Exception {
        var projectId = UUID.randomUUID();
        when(projectService.create(any())).thenReturn(
                new com.vsdya.taskflow.project.domain.Project(
                        projectId,
                        "Portfolio API",
                        "Backend portfolio project",
                        Instant.parse("2026-01-01T12:00:00Z")
                )
        );

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portfolio API\",\"description\":\"Backend portfolio project\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Portfolio API"))
                .andExpect(jsonPath("$.description").value("Backend portfolio project"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Invalid project\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {
        var projectId = UUID.randomUUID();
        when(projectService.findById(projectId)).thenThrow(new ProjectNotFoundException(projectId));

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Project not found: " + projectId))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
