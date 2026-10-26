package com.vsdya.taskflow.task.api;

import com.vsdya.taskflow.api.ApiExceptionHandler;
import com.vsdya.taskflow.task.application.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Tasks",
        description = "Create and manage tasks within projects owned by the authenticated user"
)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a task",
            description = """
                    Creates a task inside a project owned by the authenticated user.
                    The task is associated with the specified project.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or the JWT is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project does not exist or is not owned by the authenticated user",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public TaskResponse create(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return TaskResponse.from(
                taskService.create(userId, projectId, request)
        );
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(
            summary = "List tasks in a project",
            description = """
                    Returns a paginated list of tasks in a project owned by the
                    authenticated user. Tasks from other users' projects are
                    not accessible.

                    The default page size is 20 and the maximum is 100.
                    Page numbering starts at 0.
                    Use sort to order by a valid task property, for example
                    createdAt,asc or title,desc.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid sort property",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or the JWT is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project does not exist or is not owned by the authenticated user",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public Page<TaskResponse> findByProject(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID projectId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return taskService
                .findByProject(userId, projectId, pageable)
                .map(TaskResponse::from);
    }

    @GetMapping("/tasks/{id}")
    @Operation(
            summary = "Get a task",
            description = """
                    Returns a task if its project belongs to the authenticated user.
                    A missing task or a task in another user's project is not
                    accessible.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or the JWT is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task does not exist or its project is not accessible to the authenticated user",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public TaskResponse findById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return TaskResponse.from(
                taskService.findById(userId, id)
        );
    }

    @PutMapping("/tasks/{id}")
    @Operation(
            summary = "Update a task",
            description = """
                    Updates a task belonging to a project owned by the
                    authenticated user. The request includes the task title,
                    status, priority and optional description and due date.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or the JWT is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task does not exist or its project is not accessible to the authenticated user",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public TaskResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return TaskResponse.from(
                taskService.update(userId, id, request)
        );
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a task",
            description = """
                    Deletes a task belonging to a project owned by the
                    authenticated user. Tasks from other users' projects
                    cannot be deleted.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or the JWT is invalid",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task does not exist or its project is not accessible to the authenticated user",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiExceptionHandler.ErrorResponse.class
                            )
                    )
            )
    })
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        taskService.delete(userId, id);
    }
}