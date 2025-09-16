package com.gcm.integration.gca;

import com.gcm.mapper.TrainerWorkloadMapper;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.Message;
import jakarta.jms.JMSException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final WorkloadService workloadService;
    private final TrainerWorkloadMapper workloadMapper;

    @JmsListener(destination = "${workload.queue.name}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(TrainerWorkloadRequestDto request, Message message) throws JMSException {
        String transactionId = message.getStringProperty(TRANSACTION_ID_HEADER);
        MDC.put("transactionId", transactionId);

        log.info("Received workload update via JMS: {}", request);

        try {
            workloadService.processTrainerWorkload(workloadMapper.toRestModel(request));
        } finally {
            MDC.remove("transactionId");
        }
    }
}