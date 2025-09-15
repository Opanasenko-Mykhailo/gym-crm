package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.JmsMessageException;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WorkloadMessagingClientTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Captor
    private ArgumentCaptor<TrainerWorkloadRequestDto> requestCaptor;
    @Captor
    private ArgumentCaptor<MessagePostProcessor> postProcessorCaptor;
    @InjectMocks
    private WorkloadMessagingClient sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(sender, "workloadQueue", "dummy-queue");
    }

    @Test
    void givenMdcTransactionId_whenSendTrainerWorkload_thenMessageContainsThatTransactionId() throws Exception {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();
        MDC.put("transactionId", "tx-12345");

        sender.sendTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("dummy-queue"), requestCaptor.capture(), postProcessorCaptor.capture());
        Message mockMessage = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(mockMessage);
        verify(mockMessage).setStringProperty("X-Transaction-Id", "tx-12345");
    }

    @Test
    void givenNoMdcTransactionId_whenSendTrainerWorkload_thenMessageContainsGeneratedUuid() throws Exception {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();

        sender.sendTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("dummy-queue"), requestCaptor.capture(), postProcessorCaptor.capture());
        Message mockMessage = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(mockMessage);

        ArgumentCaptor<String> txCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMessage).setStringProperty(eq("X-Transaction-Id"), txCaptor.capture());
        assertThat(UUID.fromString(txCaptor.getValue())).isNotNull();
    }

    @Test
    void givenValidRequest_whenSendTrainerWorkload_thenDelegatesToJmsTemplate() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();

        sender.sendTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("dummy-queue"), eq(request), any(MessagePostProcessor.class));
    }

    @Test
    void givenJmsError_whenSendTrainerWorkload_thenThrowsJmsMessageException() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();
        doThrow(new RuntimeException("JMS error")).when(jmsTemplate).convertAndSend(anyString(), any(), any());

        JmsMessageException ex = assertThrows(JmsMessageException.class,
                () -> sender.sendTrainerWorkload(request));

        assertThat(ex.getMessage()).contains("Failed to send JMS message");
        assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    }
}