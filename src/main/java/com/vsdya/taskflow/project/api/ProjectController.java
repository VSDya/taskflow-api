package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.api.ApiExceptionHandler;
import com.vsdya.taskflow.project.application.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@RequestMapping("/api/v1/projects")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Projects",
        description = "Create and manage projects owned by the authenticated user"
)
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a project",
            description = "Creates a project owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully"
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
            )
    })
    public ProjectResponse create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return ProjectResponse.from(
                projectService.create(userId, request)
        );
    }

    @GetMapping
    @Operation(
            summary = "List my projects",
            description = """
                    Returns a paginated list of projects owned by the
                    authenticated user. Projects belonging to other users
                    are not included.

                    The default page size is 20 and the maximum is 100.
                    Use the sort parameter to sort by a valid project field,
                    for example createdAt,asc or name,desc.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully"
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
            )
    })
    public PageResponse<ProjectResponse> findAll(
            @AuthenticationPrincipal UUID userId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return PageResponse.from(
                projectService.findAll(userId, pageable)
                        .map(ProjectResponse::from)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a project",
            description = """
                    Returns a project owned by the authenticated user.
                    A project belonging to another user is reported as
                    not found.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully"
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
    public ProjectResponse findById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return ProjectResponse.from(
                projectService.findById(userId, id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a project",
            description = "Updates a project owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully"
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
    public ProjectResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ProjectResponse.from(
                projectService.update(userId, id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a project",
            description = """
                    Deletes a project owned by the authenticated user.
                    Projects belonging to other users cannot be deleted.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Project deleted successfully"
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
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        projectService.delete(userId, id);
    }
}