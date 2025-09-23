package com.gcm.validator;

import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationAspectTest {

    @Mock
    private Validator validator;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Message message;

    @Mock
    private Session session;

    @InjectMocks
    private ValidationAspect aspect;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<MessageCreator> creatorCaptor;

    private TrainerWorkloadRequestDto validDto = createValidDto();
    private TrainerWorkloadRequestDto invalidDto = createInvalidDto();

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field dlqNameField = ValidationAspect.class.getDeclaredField("dlqName");
        dlqNameField.setAccessible(true);
        dlqNameField.set(aspect, "workload.dlq");
    }

    @Test
    void givenValidDto_whenValidateMessage_thenProceedCalled() throws Throwable {
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(joinPoint.proceed()).thenReturn("Success");

        Object actual = aspect.validateMessage(joinPoint, validDto, message);

        assertEquals("Success", actual);
        verify(joinPoint).proceed();
        verify(jmsTemplate, never()).send(anyString(), any());
    }

    @Test
    void givenInvalidDto_whenValidateMessage_thenSendToDlqAndReturnNull() throws Throwable {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be empty");
        when(validator.validate(any())).thenReturn(Collections.singleton(violation));

        TextMessage dlqMessage = mock(TextMessage.class);
        when(session.createTextMessage("messageBody")).thenReturn(dlqMessage);
        when(message.getBody(String.class)).thenReturn("messageBody");
        when(message.getStringProperty("X-Transaction-Id")).thenReturn("tx123");

        Object actual = aspect.validateMessage(joinPoint, invalidDto, message);

        assertNull(actual);
        verify(jmsTemplate).send(destinationCaptor.capture(), creatorCaptor.capture());
        assertEquals("workload.dlq", destinationCaptor.getValue());
        creatorCaptor.getValue().createMessage(session);
        verify(session).createTextMessage("messageBody");
        verify(joinPoint, never()).proceed();
    }

    @Test
    void givenInvalidDtoAndDlqSendFails_whenValidateMessage_thenLogErrorAndReturnNull() throws Throwable {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be empty");

        Set<ConstraintViolation<Object>> violations = Collections.singleton(violation);
        when(validator.validate(any())).thenReturn(violations);

        doThrow(new RuntimeException("DLQ send failed")).when(jmsTemplate).send(eq("workload.dlq"), any(MessageCreator.class));

        Object actual = aspect.validateMessage(joinPoint, invalidDto, message);

        assertNull(actual);
        verify(jmsTemplate).send(eq("workload.dlq"), any(MessageCreator.class));
        verify(joinPoint, never()).proceed();
    }

    private TrainerWorkloadRequestDto createValidDto() {
        return TrainerWorkloadRequestDto.builder()
                .username("tom.cruise")
                .firstName("Tom")
                .lastName("Cruise")
                .active(true)
                .trainingDate(LocalDate.now())
                .durationInMinutes(60L)
                .actionType(TrainerWorkloadRequestDto.ActionType.ADD)
                .build();
    }

    private TrainerWorkloadRequestDto createInvalidDto() {
        return TrainerWorkloadRequestDto.builder()
                .username("")
                .firstName("Tom")
                .lastName("Cruise")
                .active(true)
                .trainingDate(LocalDate.now().plusDays(1))
                .durationInMinutes(-5L)
                .actionType(null)
                .build();
    }
}