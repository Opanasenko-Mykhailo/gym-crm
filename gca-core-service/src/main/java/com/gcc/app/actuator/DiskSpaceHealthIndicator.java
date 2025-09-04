package com.gcc.app.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        File file = new File(".");
        long freeSpace = file.getFreeSpace() / (1024 * 1024);

        if (freeSpace > 100) {
            return Health.up().withDetail("freeDiskSpaceMB", freeSpace).build();
        }

        return Health.down().withDetail("freeDiskSpaceMB", freeSpace).build();
    }
}