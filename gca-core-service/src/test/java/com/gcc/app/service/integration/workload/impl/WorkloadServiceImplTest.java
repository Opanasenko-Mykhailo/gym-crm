package com.gcc.app.service.integration.workload.impl;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WorkloadServiceImplTest {

    private static MockWebServer mockWebServer;
    private WorkloadServiceImpl service;

    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";

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
        service = new WorkloadServiceImpl(builder);
        ReflectionTestUtils.setField(service, "baseUrl", mockWebServer.url("/").toString());
    }

    @Test
    void processTrainerWorkload_givenValidRequest_whenCalled_thenSendsPostRequest() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();

        service.processTrainerWorkload(request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/workload");
        String body = recordedRequest.getBody().readUtf8();
        assertThat(body).contains("\"username\":\"" + TRAINER_USERNAME + "\"");
        assertThat(body).contains("\"firstName\":\"" + TRAINER_FIRST_NAME + "\"");
        assertThat(body).contains("\"lastName\":\"" + TRAINER_LAST_NAME + "\"");
    }

    @Test
    void getTrainerSummary_givenUsername_whenCalled_thenReturnsExpectedResponse() throws Exception {
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

        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(actual.getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
        assertThat(actual.getLastName()).isEqualTo(TRAINER_LAST_NAME);
        assertThat(actual.isActive()).isTrue();
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