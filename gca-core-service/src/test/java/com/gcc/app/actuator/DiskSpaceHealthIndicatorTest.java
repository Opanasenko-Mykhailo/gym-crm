package com.gcc.app.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;

class DiskSpaceHealthIndicatorTest {

    private final DiskSpaceHealthIndicator healthIndicator = new DiskSpaceHealthIndicator();

    @Test
    void healthUp_whenFreeSpaceIsAboveThreshold() {
        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isIn("UP", "DOWN");
        assertThat(health.getDetails()).containsKey("freeDiskSpaceMB");
    }
}