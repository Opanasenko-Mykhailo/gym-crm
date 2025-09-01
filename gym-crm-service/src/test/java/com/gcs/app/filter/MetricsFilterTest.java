package com.gcs.app.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsFilterTest {

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter serverErrorCounter;

    @Mock
    Counter loginFailureCounter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    MetricsFilter metricsFilter;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("http_server_errors_total", "status", "5xx")).thenReturn(serverErrorCounter);
        when(meterRegistry.counter("http_login_failures_total", "endpoint", "/login")).thenReturn(loginFailureCounter);

        metricsFilter = new MetricsFilter(meterRegistry);
    }

    @Test
    void givenServerErrorStatus_whenDoFilter_thenIncrementServerErrorCounter() throws IOException, ServletException {
        when(response.getStatus()).thenReturn(500);
        when(request.getRequestURI()).thenReturn("/somepath");

        metricsFilter.doFilter(request, response, filterChain);

        verify(serverErrorCounter, times(1)).increment();
        verify(loginFailureCounter, never()).increment();
    }

    @Test
    void givenLoginFailureStatusAndLoginUri_whenDoFilter_thenIncrementLoginFailureCounter() throws IOException, ServletException {
        when(response.getStatus()).thenReturn(401);
        when(request.getRequestURI()).thenReturn("/login");

        metricsFilter.doFilter(request, response, filterChain);

        verify(loginFailureCounter, times(1)).increment();
        verify(serverErrorCounter, never()).increment();
    }

    @Test
    void givenNormalStatus_whenDoFilter_thenNoCountersIncremented() throws IOException, ServletException {
        when(response.getStatus()).thenReturn(200);
        when(request.getRequestURI()).thenReturn("/somepath");

        metricsFilter.doFilter(request, response, filterChain);

        verify(serverErrorCounter, never()).increment();
        verify(loginFailureCounter, never()).increment();
    }
}