package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.MicroserviceUnavailableException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WorkloadServiceConnectorFallbackTest {

    private static final String TEST_USERNAME = "alice.smith";

    private static MockWebServer mockWebServer;

    @Autowired
    private WorkloadServiceConnector service;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setBaseUrl() {
        String mockBaseUrl = mockWebServer.url("/").toString();
        ReflectionTestUtils.setField(service, "baseUrl", mockBaseUrl);
    }

    @Test
    void givenWorkloadServiceReturns500_whenGetTrainerSummary_thenFallbackThrowsServiceUnavailable() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> service.getTrainerSummary(TEST_USERNAME))
                .isInstanceOf(MicroserviceUnavailableException.class)
                .hasMessageContaining("Workload service is temporarily unavailable for retrieving trainer summary");
    }
}