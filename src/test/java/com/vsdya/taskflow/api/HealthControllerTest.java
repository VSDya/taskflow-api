package com.vsdya.taskflow.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void shouldReturnHealthyStatus() {
        Map<String, String> response = healthController.health();

        assertEquals("UP", response.get("status"));
        assertEquals("taskflow-api", response.get("service"));
        assertEquals(2, response.size());
    }
}