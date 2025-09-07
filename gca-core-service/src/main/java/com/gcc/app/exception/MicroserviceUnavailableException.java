package com.gcc.app.exception;

public class MicroserviceUnavailableException extends RuntimeException {
    public MicroserviceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}