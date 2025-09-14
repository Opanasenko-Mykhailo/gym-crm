package com.gcm.service.integration.gca;

import com.gcm.mapper.TrainerWorkloadMapper;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import jakarta.jms.Message;
import jakarta.jms.JMSException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadConsumerService {

    private final WorkloadService workloadService;

    private final TrainerWorkloadMapper workloadMapper;

    @JmsListener(destination = "${workload.queue.name}", containerFactory = "jmsListenerContainerFactory")
    public void receiveTrainerWorkload(TrainerWorkloadRequestDto request, Message message) throws JMSException {
        String transactionId = message.getStringProperty("X-Transaction-Id");
        MDC.put("transactionId", transactionId);

        log.info("Received workload update from JMS: {}", request);

        try {
            workloadService.processTrainerWorkload(workloadMapper.toRestModel(request));
        } catch (Exception ex) {
            log.error("Failed to process trainer workload ", ex);
            throw ex;
        } finally {
            MDC.remove("transactionId");
        }
    }
}