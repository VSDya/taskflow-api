package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.security.JwtService;
import com.vsdya.taskflow.task.application.TaskService;
import com.vsdya.taskflow.task.domain.Task;
import com.vsdya.taskflow.task.domain.TaskPriority;
import com.vsdya.taskflow.task.domain.TaskStatus;
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

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldCreateTaskForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        when(taskService.create(
                eq(userId),
                eq(projectId),
                any(CreateTaskRequest.class)
        )).thenReturn(
                new Task(
                        taskId,
                        projectId,
                        "Implement authentication",
                        "Add JWT authentication",
                        TaskStatus.TODO,
                        TaskPriority.MEDIUM,
                        null,
                        Instant.parse("2026-01-01T12:00:00Z"),
                        Instant.parse("2026-01-01T12:00:00Z")
                )
        );

        mockMvc.perform(
                        post("/api/v1/projects/{projectId}/tasks", projectId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Implement authentication",
                                            "description": "Add JWT authentication",
                                            "priority": "MEDIUM",
                                            "dueDate": null
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.title")
                        .value("Implement authentication"));
    }

    @Test
    void shouldGetTaskForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(taskService.findById(userId, taskId))
                .thenReturn(
                        new Task(
                                taskId,
                                projectId,
                                "My task",
                                "My task description",
                                TaskStatus.TODO,
                                TaskPriority.MEDIUM,
                                null,
                                Instant.parse("2026-01-01T12:00:00Z"),
                                Instant.parse("2026-01-01T12:00:00Z")
                        )
                );

        mockMvc.perform(
                        get("/api/v1/tasks/{id}", taskId)
                                .with(asUser(userId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.projectId")
                        .value(projectId.toString()))
                .andExpect(jsonPath("$.title").value("My task"));
    }

    @Test
    void shouldRejectBlankTaskTitle() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/projects/{projectId}/tasks", projectId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "description": "Invalid task",
                                            "priority": "MEDIUM",
                                            "dueDate": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
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