package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.JmsMessageException;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadMessagingClient {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final JmsTemplate jmsTemplate;

    @Value("${workload.queue.name}")
    private String workloadQueue;

    public void sendTrainerWorkload(TrainerWorkloadRequestDto request) {
        String transactionId = getCurrentTransactionId();
        log.info("Sending trainer workload request via JMS: {}", request);

        try {
            jmsTemplate.convertAndSend(workloadQueue, request,
                    message -> enrichWithTransactionId(message, transactionId));
        } catch (Exception ex) {
            throw new JmsMessageException("Failed to send JMS message for trainer workload", ex);
        }
    }

    private Message enrichWithTransactionId(Message message, String transactionId) throws JMSException {
        message.setStringProperty(TRANSACTION_ID_HEADER, transactionId);

        return message;
    }

    private String getCurrentTransactionId() {
        String transactionId = MDC.get("transactionId");

        return transactionId != null
                ? transactionId
                : UUID.randomUUID().toString();
    }
}