package com.gcs.app.exception;

public class EntityNotFoundException extends DaoException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}