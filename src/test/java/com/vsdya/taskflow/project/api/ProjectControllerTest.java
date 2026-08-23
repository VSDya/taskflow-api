package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.application.ProjectService;
import com.vsdya.taskflow.project.domain.Project;
import com.vsdya.taskflow.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldCreateProject() throws Exception {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectService.create(
                eq(userId),
                any(CreateProjectRequest.class)
        )).thenReturn(
                new Project(
                        projectId,
                        "Portfolio API",
                        "Backend portfolio project",
                        Instant.parse("2026-01-01T12:00:00Z")
                )
        );

        mockMvc.perform(
                        post("/api/v1/projects")
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Portfolio API",
                                            "description": "Backend portfolio project"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Portfolio API"))
                .andExpect(jsonPath("$.description")
                        .value("Backend portfolio project"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectBlankName() throws Exception {
        var userId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/projects")
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "",
                                            "description": "Invalid project"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectService.findById(userId, projectId))
                .thenThrow(new ProjectNotFoundException(projectId));

        mockMvc.perform(
                        get("/api/v1/projects/{id}", projectId)
                                .with(asUser(userId))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Project not found: " + projectId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private RequestPostProcessor asUser(UUID userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null
                    )
            );
            return request;
        };
    }
}