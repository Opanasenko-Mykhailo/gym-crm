package com.gcm.integration.gca;

import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.mapper.TrainerWorkloadMapper;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

    @Mock
    private WorkloadService workloadService;

    @Mock
    private TrainerWorkloadMapper workloadMapper;

    @Mock
    private Message jmsMessage;

    @Captor
    private ArgumentCaptor<TrainerWorkloadRequest> requestCaptor;

    @InjectMocks
    private TrainerWorkloadMessageListener listener;

    @Test
    void receiveTrainerWorkload_processesRequestSuccessfully() throws JMSException {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setUsername("trainer.jane");
        TrainerWorkloadRequest restModel = new TrainerWorkloadRequest();
        restModel.setUsername("trainer.jane");

        when(workloadMapper.toRestModel(dto)).thenReturn(restModel);
        when(jmsMessage.getStringProperty("X-Transaction-Id")).thenReturn("txn-123");

        listener.onMessage(dto, jmsMessage);

        verify(workloadMapper).toRestModel(dto);
        verify(workloadService).processTrainerWorkload(requestCaptor.capture());
        assertEquals("trainer.jane", requestCaptor.getValue().getUsername());
        assertNull(MDC.get("transactionId"), "MDC should be cleared after processing");
    }

    @Test
    void receiveTrainerWorkload_logsAndThrowsOnException() throws JMSException {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        TrainerWorkloadRequest restModel = new TrainerWorkloadRequest();

        when(workloadMapper.toRestModel(dto)).thenReturn(restModel);
        when(jmsMessage.getStringProperty("X-Transaction-Id")).thenReturn("txn-456");
        doThrow(new RuntimeException("Processing failed")).when(workloadService).processTrainerWorkload(restModel);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> listener.onMessage(dto, jmsMessage));

        assertEquals("Processing failed", ex.getMessage());
        assertNull(MDC.get("transactionId"), "MDC should be cleared after exception");
    }
}