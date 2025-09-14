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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

class WorkloadServiceConnectorTest {

    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";

    private static MockWebServer mockWebServer;

    @Mock
    private JmsTemplate jmsTemplate;

    @Captor
    private ArgumentCaptor<TrainerWorkloadRequestDto> requestCaptor;

    @Captor
    private ArgumentCaptor<MessagePostProcessor> postProcessorCaptor;

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
        MockitoAnnotations.openMocks(this);
        WebClient.Builder builder = WebClient.builder();
        service = new WorkloadServiceConnector(builder, jmsTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(service, "workloadQueue", "dummy-queue");
        SecurityContextHolder.clearContext();
    }

    @Test
    void processTrainerWorkload_givenValidRequest_whenCalled_thenSendsViaJms() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TRAINER_USERNAME, "dummy-token")
        );
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();
        doNothing().when(jmsTemplate).convertAndSend(anyString(), any(TrainerWorkloadRequestDto.class), any(MessagePostProcessor.class));

        service.processTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("dummy-queue"), requestCaptor.capture(), postProcessorCaptor.capture());

        TrainerWorkloadRequestDto captured = requestCaptor.getValue();
        assertThat(captured.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(captured.getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
        assertThat(captured.getLastName()).isEqualTo(TRAINER_LAST_NAME);
    }

    @Test
    void processTrainerWorkload_givenNoToken_stillSendsJms() {
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();
        doNothing().when(jmsTemplate).convertAndSend(anyString(), any(TrainerWorkloadRequestDto.class), any(MessagePostProcessor.class));

        service.processTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq((String) ReflectionTestUtils.getField(service, "workloadQueue")), requestCaptor.capture(), postProcessorCaptor.capture());
        assertThat(requestCaptor.getValue()).isEqualTo(request);
    }

    @Test
    void getTrainerSummary_givenValidUsernameWithToken_returnsResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TRAINER_USERNAME, "dummy-token")
        );

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
    void getTrainerSummary_givenNoToken_throwsUserNotAuthenticatedException() {
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