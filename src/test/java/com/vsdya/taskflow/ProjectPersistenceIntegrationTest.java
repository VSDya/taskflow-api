package com.vsdya.taskflow;

import com.vsdya.taskflow.project.infrastructure.ProjectEntity;
import com.vsdya.taskflow.project.infrastructure.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ProjectPersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldPersistAndReadProjectFromPostgres() {
        var id = UUID.randomUUID();
        projectRepository.save(new ProjectEntity(
                id,
                "Integration Test Project",
                "Stored in PostgreSQL",
                Instant.now()
        ));

        var stored = projectRepository.findById(id);

        assertThat(stored).isPresent();
        assertThat(stored.get().getName()).isEqualTo("Integration Test Project");
        assertThat(stored.get().getDescription()).isEqualTo("Stored in PostgreSQL");
    }
}
