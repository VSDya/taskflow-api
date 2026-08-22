package com.vsdya.taskflow.project.api;

import com.vsdya.taskflow.project.application.ProjectService;
import com.vsdya.taskflow.project.domain.Project;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.create(request);
        return ProjectResponse.from(project);
    }

    @GetMapping
    public Page<ProjectResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return projectService.findAll(pageable).map(ProjectResponse::from);
    }

    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable UUID id) {
        return ProjectResponse.from(projectService.findById(id));
    }
}
