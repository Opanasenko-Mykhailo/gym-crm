package com.gcc.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
enum ApiError {

    INVALID_REQUEST_ERROR(2400, "Invalid request: ", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(2760, "Validation error: ", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_ERROR(2805, "Authentication fails", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_ERROR(2806, "User is not authorized for request operation", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID_ERROR(2807, "Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(2808, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED_ERROR(2810, "Access denied: insufficient permissions", HttpStatus.FORBIDDEN),
    NOT_FOUND_ERROR(2835, "Requested data was not found: ", HttpStatus.NOT_FOUND),
    BRUTE_FORCE_BLOCKED(2901,  "Too many failed login attempts, try again later", HttpStatus.TOO_MANY_REQUESTS),
    SERVER_ERROR(3200, "Internal processing error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(3358, "Unexpected database access failure", HttpStatus.INTERNAL_SERVER_ERROR),
    MICROSERVICE_UNAVAILABLE(3500, "External service is temporarily unavailable: ", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiError(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}