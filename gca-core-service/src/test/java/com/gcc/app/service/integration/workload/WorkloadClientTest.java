package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadClientTest {

    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";

    @Mock
    private WorkloadClient client;

    @Test
    void processWorkload_givenRequest_whenCalled_thenClientInvoked() {
        TrainerWorkloadRequestDto request = createTrainerWorkloadRequestDto();

        client.processWorkload(request);

        verify(client).processWorkload(request);
    }

    @Test
    void getTrainerSummary_givenUsername_whenCalled_thenReturnsResponse() {
        TrainerSummaryResponseDto response = createTrainerSummaryResponseDto();
        when(client.getTrainerSummary(TRAINER_USERNAME)).thenReturn(response);

        TrainerSummaryResponseDto actual = client.getTrainerSummary(TRAINER_USERNAME);

        verify(client).getTrainerSummary(TRAINER_USERNAME);
        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(actual.getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
        assertThat(actual.getLastName()).isEqualTo(TRAINER_LAST_NAME);
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

    private TrainerSummaryResponseDto createTrainerSummaryResponseDto() {
        return TrainerSummaryResponseDto.builder()
                .username(TRAINER_USERNAME)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .active(true)
                .build();
    }
}