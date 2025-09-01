package com.gcs.app.exception;

import com.gcs.app.rest.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static com.gcs.app.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gcs.app.exception.ApiError.DATABASE_ERROR;
import static com.gcs.app.exception.ApiError.INVALID_REQUEST_ERROR;
import static com.gcs.app.exception.ApiError.NOT_FOUND_ERROR;
import static com.gcs.app.exception.ApiError.SERVER_ERROR;
import static com.gcs.app.exception.ApiError.VALIDATION_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final Set<String> BAD_REQUEST_PREFIXES = Set.of(
            "Invalid trainee username",
            "Invalid trainer username",
            "Trainee username must be provided",
            "Trainer username must be provided",
            "Username must not be null",
            "Invalid training type"
    );

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex) {
        log.error("ServiceException: {}", ex.getMessage(), ex);
        ApiError apiError = resolveError(ex);

        return apiError == INVALID_REQUEST_ERROR
                ? buildErrorResponse(apiError, ex.getMessage())
                : buildErrorResponse(apiError);
    }

    @ExceptionHandler(DaoException.class)
    public ResponseEntity<ErrorResponse> handleDaoException(DaoException ex) {
        log.error("Database Exception: {}", ex.getMessage(), ex);

        return buildErrorResponse(DATABASE_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("EntityNotFoundException: {}", ex.getMessage(), ex);

        return buildErrorResponse(NOT_FOUND_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        String message = extractValidationMessage(ex);

        return buildErrorResponse(VALIDATION_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage(), ex);

        return buildErrorResponse(VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthenticatedException(UserNotAuthenticatedException ex) {
        log.error("UserNotAuthenticatedException: {}", ex.getMessage(), ex);

        return buildErrorResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthorizedException(UserNotAuthorizedException ex) {
        log.error("UserNotAuthorizedException: {}", ex.getMessage(), ex);

        return buildErrorResponse(ApiError.AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(RuntimeException ex) {
        log.error("Unhandled RuntimeException: {}", ex.getMessage(), ex);

        return buildErrorResponse(SERVER_ERROR);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.error("AuthorizationDeniedException: {}", ex.getMessage(), ex);

        return buildErrorResponse(ApiError.ACCESS_DENIED_ERROR);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        log.error("RefreshTokenNotFoundException: {}", ex.getMessage());

        return buildErrorResponse(ApiError.TOKEN_INVALID_ERROR);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlockedException(UserBlockedException ex) {
        log.error("UserBlockedException: {}", ex.getMessage(), ex);

        return buildErrorResponse(ApiError.BRUTE_FORCE_BLOCKED);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.error("InvalidCredentialsException: {}", ex.getMessage(), ex);

        return buildErrorResponse(ApiError.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);

        return buildErrorResponse(INVALID_REQUEST_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError) {
        return buildErrorResponse(apiError, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError, String details) {
        String baseMessage = apiError.getMessage();
        String errorDetails = isNotBlank(details) ? details : "";

        ErrorResponse errorResponse = new ErrorResponse(apiError.getCode(), baseMessage + errorDetails);

        return new ResponseEntity<>(errorResponse, apiError.getHttpStatus());
    }

    private ApiError resolveError(ServiceException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";

        return BAD_REQUEST_PREFIXES.stream()
                .filter(prefix -> message.toLowerCase().startsWith(prefix.toLowerCase()))
                .findFirst()
                .map(x -> INVALID_REQUEST_ERROR)
                .orElse(SERVER_ERROR);
    }

    private String extractValidationMessage(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(msg -> !msg.isBlank())
                .findFirst()
                .orElse("Validation failed");
    }
}