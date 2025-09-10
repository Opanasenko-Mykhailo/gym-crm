package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.UserNotAuthenticatedException;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
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

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkloadServiceConnectorTest {

    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";

    private static MockWebServer mockWebServer;

    private WorkloadServiceConnector service;

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
    void setUp() {
        WebClient.Builder builder = WebClient.builder();
        service = new WorkloadServiceConnector(builder);
        ReflectionTestUtils.setField(service, "baseUrl", mockWebServer.url("/").toString());
        SecurityContextHolder.clearContext();
    }

    @Test
    void processTrainerWorkload_givenValidRequestWithToken_whenCalled_thenSendsPostRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(TRAINER_USERNAME, "dummy-token"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();

        service.processTrainerWorkload(request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/workload");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer dummy-token");

        String body = recordedRequest.getBody().readUtf8();
        assertThat(body).contains("\"username\":\"" + TRAINER_USERNAME + "\"");
        assertThat(body).contains("\"firstName\":\"" + TRAINER_FIRST_NAME + "\"");
        assertThat(body).contains("\"lastName\":\"" + TRAINER_LAST_NAME + "\"");
    }

    @Test
    void getTrainerSummary_givenValidUsernameWithToken_whenCalled_thenReturnsExpectedResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(TRAINER_USERNAME, "dummy-token"));
        String responseJson = """
                {
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "active": true,
                  "years": []
                }
                """.formatted(TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        TrainerSummaryResponseDto actual = service.getTrainerSummary(TRAINER_USERNAME);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/workload/" + TRAINER_USERNAME);
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer dummy-token");

        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(actual.getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
        assertThat(actual.getLastName()).isEqualTo(TRAINER_LAST_NAME);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void processTrainerWorkload_givenNoToken_thenThrowsUserNotAuthenticatedException() {
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();

        assertThatThrownBy(() -> service.processTrainerWorkload(request))
                .isInstanceOf(UserNotAuthenticatedException.class)
                .hasMessageContaining("No JWT token found for current user");
    }

    @Test
    void getTrainerSummary_givenNoToken_thenThrowsUserNotAuthenticatedException() {
        assertThatThrownBy(() -> service.getTrainerSummary(TRAINER_USERNAME))
                .isInstanceOf(UserNotAuthenticatedException.class)
                .hasMessageContaining("No JWT token found for current user");
    }

    private TrainerWorkloadRequestDto createTrainerWorkloadRequestDto() {
        return TrainerWorkloadRequestDto.builder()
                .username(TRAINER_USERNAME)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .active(true)
                .trainingDate(LocalDate.of(2025, 9, 5))
                .durationInMinutes(90L)
                .actionType(TrainerWorkloadRequestDto.ActionType.ADD)
                .build();
    }
}