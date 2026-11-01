
package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.application.ProjectNotFoundException;
import com.vsdya.taskflow.project.application.ProjectService;
import com.vsdya.taskflow.project.domain.Project;
import com.vsdya.taskflow.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
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

    @Test
    void shouldReturnProjects() throws Exception {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        var project = new Project(
                projectId,
                "Portfolio API",
                "Backend portfolio project",
                Instant.parse("2026-01-01T12:00:00Z")
        );

        var pageable = PageRequest.of(
                0,
                20,
                Sort.by("createdAt")
        );

        when(projectService.findAll(
                eq(userId),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of(project), pageable, 1)
        );

        mockMvc.perform(
                        get("/api/v1/projects")
                                .with(asUser(userId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(projectId.toString()))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Portfolio API"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldUpdateProject() throws Exception {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        when(projectService.update(
                eq(userId),
                eq(projectId),
                any(UpdateProjectRequest.class)
        )).thenReturn(
                new Project(
                        projectId,
                        "Updated Portfolio",
                        "Updated description",
                        Instant.parse("2026-01-01T12:00:00Z")
                )
        );

        mockMvc.perform(
                        put("/api/v1/projects/{id}", projectId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Updated Portfolio",
                                            "description": "Updated description"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Portfolio"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldDeleteProject() throws Exception {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/v1/projects/{id}", projectId)
                                .with(asUser(userId))
                )
                .andExpect(status().isNoContent());

        verify(projectService).delete(userId, projectId);
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
