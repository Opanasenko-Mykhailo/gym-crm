package com.gcm.validator;

import jakarta.jms.Message;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationAspect {

    private final Validator validator;
    private final JmsTemplate jmsTemplate;

    @Value("${workload.dlq.name}")
    private String dlqName;

    @Around("@annotation(ValidateMessage) && args(request,message,..)")
    public Object validateMessage(ProceedingJoinPoint joinPoint, Object request, Message message) throws Throwable {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String reason = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            log.error("Validation failed for message, sending to DLQ: {}", reason);
            sendToDlq(message, "VALIDATION_FAILED: " + reason);

            return null;
        }

        return joinPoint.proceed();
    }

    private void sendToDlq(Message originalMessage, String reason) {
        try {
            jmsTemplate.send(dlqName, session -> {
                String messageBody = originalMessage.getBody(String.class);
                Message dlqMessage = session.createTextMessage(messageBody);
                dlqMessage.setStringProperty("X-Transaction-Id", originalMessage.getStringProperty("X-Transaction-Id"));
                dlqMessage.setStringProperty("ERROR_REASON", reason);
                dlqMessage.setLongProperty("ERROR_TIMESTAMP", System.currentTimeMillis());

                return dlqMessage;});
        } catch (Exception e) {
            log.error("Failed to send to DLQ", e);
        }
    }
}