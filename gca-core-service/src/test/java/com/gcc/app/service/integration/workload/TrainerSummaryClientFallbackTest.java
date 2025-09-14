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
class TrainerSummaryClientFallbackTest {

    private static final String TEST_USERNAME = "alice.smith";
    private static MockWebServer mockWebServer;

    @Autowired
    private TrainerSummaryClient client;

    @BeforeAll
    static void setupServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        String mockBaseUrl = mockWebServer.url("/").toString();
        ReflectionTestUtils.setField(client, "baseUrl", mockBaseUrl);
    }

    @Test
    void getTrainerSummary_whenServiceReturns500_thenFallbackThrowsMicroserviceUnavailable() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> client.getTrainerSummary(TEST_USERNAME))
                .isInstanceOf(MicroserviceUnavailableException.class)
                .hasMessageContaining("Workload service is temporarily unavailable for retrieving trainer summary");
    }
}