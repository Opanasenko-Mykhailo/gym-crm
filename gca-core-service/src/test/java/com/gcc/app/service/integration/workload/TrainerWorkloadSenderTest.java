package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.JmsMessageException;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class TrainerWorkloadSenderTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Captor
    private ArgumentCaptor<TrainerWorkloadRequestDto> requestCaptor;
    @Captor
    private ArgumentCaptor<MessagePostProcessor> postProcessorCaptor;
    private TrainerWorkloadSender sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sender = new TrainerWorkloadSender(jmsTemplate);
        ReflectionTestUtils.setField(sender, "workloadQueue", "dummy-queue");
    }

    @Test
    void sendTrainerWorkload_sendsMessageSuccessfully() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();
        doNothing().when(jmsTemplate).convertAndSend(anyString(), any(), any());

        sender.sendTrainerWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("dummy-queue"), requestCaptor.capture(), postProcessorCaptor.capture());
        assertThat(requestCaptor.getValue()).isEqualTo(request);
    }

    @Test
    void sendTrainerWorkload_jmsFails_throwsException() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();
        doThrow(new RuntimeException("JMS error")).when(jmsTemplate).convertAndSend(anyString(), any(), any());

        assertThatThrownBy(() -> sender.sendTrainerWorkload(request))
                .isInstanceOf(JmsMessageException.class)
                .hasMessageContaining("Failed to send JMS message");
    }

    @Test
    void sendTrainerWorkload_jmsFails_throwsExceptionWithCause() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder().build();
        RuntimeException jmsEx = new RuntimeException("JMS error");
        doThrow(jmsEx).when(jmsTemplate).convertAndSend(anyString(), any(), any());

        JmsMessageException thrown = assertThrows(JmsMessageException.class,
                () -> sender.sendTrainerWorkload(request));

        assertThat(thrown).hasMessageContaining("Failed to send JMS message");
        assertThat(thrown.getCause()).isEqualTo(jmsEx);
    }
}