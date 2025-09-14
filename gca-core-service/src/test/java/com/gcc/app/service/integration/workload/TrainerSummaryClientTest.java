package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.UserNotAuthenticatedException;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainerSummaryClientTest {

    private static MockWebServer mockWebServer;
    private TrainerSummaryClient client;

    @BeforeAll
    static void setupServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws Exception {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = WebClient.builder();
        client = new TrainerSummaryClient(builder);
        ReflectionTestUtils.setField(client, "baseUrl", mockWebServer.url("/").toString());
        SecurityContextHolder.clearContext();
    }

    @Test
    void getTrainerSummary_withToken_returnsResponse() throws Exception {
        String username = "alice.smith";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, "dummy-token"));

        String responseJson = """
                {"username":"alice.smith","firstName":"Alice","lastName":"Smith","active":true,"years":[]}
                """;

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(responseJson).addHeader("Content-Type", "application/json"));

        TrainerSummaryResponseDto result = client.getTrainerSummary(username);

        RecordedRequest actual = mockWebServer.takeRequest();
        assertThat(actual.getHeader("Authorization")).isEqualTo("Bearer dummy-token");
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    void getTrainerSummary_noToken_throwsException() {
        assertThatThrownBy(() -> client.getTrainerSummary("alice.smith"))
                .isInstanceOf(UserNotAuthenticatedException.class)
                .hasMessageContaining("No JWT token found");
    }
}