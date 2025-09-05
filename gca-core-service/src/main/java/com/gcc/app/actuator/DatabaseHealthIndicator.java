package com.gcc.app.actuator;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final String DATABASE = "database";

    private final HikariDataSource hikariDataSource;

    @Override
    public Health health() {
        try {
            boolean isRunning = hikariDataSource.isRunning();
            int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();

            return isRunning
                    ? Health.up().withDetail(DATABASE, "Available")
                    .withDetail("activeConnections", activeConnections)
                    .withDetail("totalConnections", totalConnections)
                    .build()
                    : Health.down()
                    .withDetail(DATABASE, "HikariPool not running")
                    .build();
        } catch (Exception e) {
            return Health.down(e).withDetail(DATABASE, "Unavailable").build();
        }
    }
}