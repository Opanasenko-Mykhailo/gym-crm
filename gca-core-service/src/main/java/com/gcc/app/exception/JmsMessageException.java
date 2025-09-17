package com.gcc.app.exception;

public class JmsMessageException extends RuntimeException {
    public JmsMessageException(String message) {
        super(message);
    }

    public JmsMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}