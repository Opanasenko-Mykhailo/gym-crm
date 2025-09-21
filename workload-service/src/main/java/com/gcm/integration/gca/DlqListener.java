package com.gcm.integration.gca;

import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlqListener {

    @JmsListener(destination = "${workload.dlq.name}", containerFactory = "jmsListenerContainerFactory")
    public void onDlqMessage(Message message) {
            log.error("Received message in DLQ: {}", message);
    }
}