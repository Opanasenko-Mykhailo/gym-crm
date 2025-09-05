package com.gcc.app.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MemoryHealthIndicator implements HealthIndicator {

    private static final long MAX_MEMORY_THRESHOLD_MB = 2000;

    @Override
    public Health health() {
        long heapMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long usedMemory = heapMemory - freeMemory;

        Health.Builder healthBuilder = (usedMemory < MAX_MEMORY_THRESHOLD_MB)
                ? Health.up()
                : Health.down();

        return healthBuilder
                .withDetail("usedMemoryMB", usedMemory)
                .withDetail("freeMemoryMB", freeMemory)
                .withDetail("heapMemoryMB", heapMemory)
                .withDetail("maxMemoryMB", maxMemory)
                .withDetail("thresholdMB", MAX_MEMORY_THRESHOLD_MB)
                .build();
    }
}