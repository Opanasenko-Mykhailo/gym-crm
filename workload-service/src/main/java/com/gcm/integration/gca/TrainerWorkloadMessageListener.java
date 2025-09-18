package com.gcm.integration.gca;

import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final WorkloadService workloadService;

    @JmsListener(destination = "${workload.queue.name}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(TrainerWorkloadRequestDto request, Message message) throws JMSException {
        String transactionId = message.getStringProperty(TRANSACTION_ID_HEADER);
        MDC.put("transactionId", transactionId);

        log.info("Received workload update via JMS: {}", request);

        try {
            workloadService.processTrainerWorkload(request);
        } finally {
            MDC.remove("transactionId");
        }
    }
}