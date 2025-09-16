package com.gcc.app.integration.workload;

import com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private WorkloadMessageSender messageSender;
    @Mock
    private WorkloadHttpClient httpClient;
    @InjectMocks
    private WorkloadService service;

    @Test
    void processTrainerWorkload_delegatesToSender() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();

        service.notifyWorkloadChange(request);

        verify(messageSender).sendTrainerWorkload(request);
    }

    @Test
    void getTrainerSummary_delegatesToClient() {
        String username = "alice.smith";

        service.getTrainerSummary(username);

        verify(httpClient).getTrainerSummary(username);
    }
}