package com.vsdya.taskflow;

import com.vsdya.taskflow.auth.infrastructure.UserEntity;
import com.vsdya.taskflow.auth.infrastructure.UserRepository;
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
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldPersistAndReadProjectFromPostgres() {
        var ownerId = UUID.randomUUID();

        userRepository.save(new UserEntity(
                ownerId,
                "owner@example.com",
                "hashed-password",
                Instant.now()
        ));

        var projectId = UUID.randomUUID();

        projectRepository.save(new ProjectEntity(
                projectId,
                ownerId,
                "Integration Test Project",
                "Stored in PostgreSQL",
                Instant.now()
        ));

        var stored = projectRepository.findByIdAndOwnerId(
                projectId,
                ownerId
        );

        assertThat(stored).isPresent();
        assertThat(stored.get().getOwnerId()).isEqualTo(ownerId);
        assertThat(stored.get().getName())
                .isEqualTo("Integration Test Project");
        assertThat(stored.get().getDescription())
                .isEqualTo("Stored in PostgreSQL");
    }
}