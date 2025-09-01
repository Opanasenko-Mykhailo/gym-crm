package com.gcs.app.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "metrics.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsFilter implements Filter {

    private final Counter serverErrorCounter;
    private final Counter loginFailureCounter;

    public MetricsFilter(MeterRegistry meterRegistry) {
        this.serverErrorCounter = meterRegistry.counter("http_server_errors_total", "status", "5xx");
        this.loginFailureCounter = meterRegistry.counter("http_login_failures_total", "endpoint", "/login");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        chain.doFilter(request, response);

        int status = httpResponse.getStatus();
        String path = httpRequest.getRequestURI();

        if (isServerErrorStatus(status)) {
            serverErrorCounter.increment();
        }

        if (isLoginFailure(path, status)) {
            loginFailureCounter.increment();
        }
    }

    private boolean isServerErrorStatus(int status) {
        return status >= 500 && status < 600;
    }

    private boolean isLoginFailure(String uri, int status) {
        return uri.contains("/login") && (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN);
    }
}