package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceConnectorTest {

    @Mock
    private TrainerWorkloadSender workloadSender;
    @Mock
    private TrainerSummaryClient summaryClient;
    @InjectMocks
    private WorkloadServiceConnector connector;

    @Test
    void processTrainerWorkload_delegatesToSender() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();

        connector.processTrainerWorkload(request);

        verify(workloadSender).sendTrainerWorkload(request);
    }

    @Test
    void getTrainerSummary_delegatesToClient() {
        String username = "alice.smith";

        connector.getTrainerSummary(username);

        verify(summaryClient).getTrainerSummary(username);
    }
}