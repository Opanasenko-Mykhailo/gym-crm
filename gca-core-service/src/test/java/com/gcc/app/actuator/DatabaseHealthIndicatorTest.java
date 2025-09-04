package com.gcc.app.actuator;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private HikariDataSource hikariDataSource;

    @Mock
    private HikariPoolMXBean hikariPoolMXBean;

    @InjectMocks
    private DatabaseHealthIndicator healthIndicator;

    @Test
    void healthUp_whenPoolIsRunning() {
        when(hikariDataSource.isRunning()).thenReturn(true);
        when(hikariDataSource.getHikariPoolMXBean()).thenReturn(hikariPoolMXBean);
        when(hikariPoolMXBean.getActiveConnections()).thenReturn(5);
        when(hikariPoolMXBean.getTotalConnections()).thenReturn(10);

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("database", "Available");
        assertThat(health.getDetails()).containsEntry("activeConnections", 5);
        assertThat(health.getDetails()).containsEntry("totalConnections", 10);
    }

    @Test
    void healthDown_whenPoolIsNotRunning() {
        when(hikariDataSource.isRunning()).thenReturn(false);
        when(hikariDataSource.getHikariPoolMXBean()).thenReturn(hikariPoolMXBean);

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsEntry("database", "HikariPool not running");
    }

    @Test
    void healthDown_whenExceptionThrown() {
        when(hikariDataSource.isRunning()).thenThrow(new RuntimeException("DB error"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsEntry("database", "Unavailable");
        assertThat(health.getDetails()).containsKey("error");
    }
}