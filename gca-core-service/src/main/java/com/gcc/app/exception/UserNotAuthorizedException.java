package com.gcc.app.exception;

public class UserNotAuthorizedException extends RuntimeException {
    public UserNotAuthorizedException(String message) {
        super(message);
    }

    public UserNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}