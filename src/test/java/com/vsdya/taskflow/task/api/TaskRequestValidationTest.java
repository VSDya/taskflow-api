package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.security.JwtService;
import com.vsdya.taskflow.task.application.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldRejectTaskTitleLongerThanTwoHundredCharacters()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        String title = "a".repeat(201);

        mockMvc.perform(
                        post("/api/v1/projects/{projectId}/tasks", projectId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "%s",
                                            "description": "Too long",
                                            "priority": "MEDIUM",
                                            "dueDate": null
                                        }
                                        """.formatted(title))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectMissingRequiredStatusWhenUpdatingTask()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(
                        put("/api/v1/tasks/{id}", taskId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Valid title",
                                            "description": "Valid description",
                                            "status": null,
                                            "priority": "HIGH",
                                            "dueDate": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectUnknownTaskStatusWhenUpdatingTask()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(
                        put("/api/v1/tasks/{id}", taskId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Valid title",
                                            "description": "Valid description",
                                            "status": "UNKNOWN_STATUS",
                                            "priority": "HIGH",
                                            "dueDate": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_JSON"));
    }

    @Test
    void shouldRejectInvalidDueDateWhenCreatingTask()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/projects/{projectId}/tasks", projectId)
                                .with(asUser(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Valid title",
                                            "description": "Valid description",
                                            "priority": "MEDIUM",
                                            "dueDate": "31-12-2026"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_JSON"));
    }

    private RequestPostProcessor asUser(UUID userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null)
            );
            return request;
        };
    }
}
