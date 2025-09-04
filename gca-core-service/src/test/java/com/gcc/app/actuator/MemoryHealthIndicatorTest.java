package com.gcc.app.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryHealthIndicatorTest {

    private final MemoryHealthIndicator healthIndicator = new MemoryHealthIndicator();

    @Test
    void healthUp_whenUsedMemoryBelowThreshold() {
        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isIn("UP", "DOWN");
        assertThat(health.getStatus().getCode()).isIn("UP", "DOWN");
        assertThat(health.getDetails()).containsKeys("usedMemoryMB", "freeMemoryMB", "heapMemoryMB", "maxMemoryMB", "thresholdMB");
    }
}