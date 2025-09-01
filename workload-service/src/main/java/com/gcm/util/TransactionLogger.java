package com.gcm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionLogger {

    public void log(String transactionId, String message) {
        log.info("[transactionId={}]: {}", transactionId, message);
    }
}