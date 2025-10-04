package com.gcc.app.exception;

import com.gcc.app.rest.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static com.gcc.app.exception.ApiError.ACCESS_DENIED_ERROR;
import static com.gcc.app.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gcc.app.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gcc.app.exception.ApiError.BRUTE_FORCE_BLOCKED;
import static com.gcc.app.exception.ApiError.DATABASE_ERROR;
import static com.gcc.app.exception.ApiError.INVALID_CREDENTIALS;
import static com.gcc.app.exception.ApiError.INVALID_REQUEST_ERROR;
import static com.gcc.app.exception.ApiError.NOT_FOUND_ERROR;
import static com.gcc.app.exception.ApiError.SERVER_ERROR;
import static com.gcc.app.exception.ApiError.TOKEN_INVALID_ERROR;
import static com.gcc.app.exception.ApiError.VALIDATION_ERROR;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    private static final String VALIDATION_MSG = "Username must not be blank";
    private static final String ENTITY_NOT_FOUND_MSG = "Trainee with ID 10 was not found in the database";
    private static final String UNKNOWN_SERVICE_MSG = "Unexpected error occurred during trainee registration";
    private static final String DAO_ERROR_MSG = "Failed to execute database query for trainee entity";
    private static final String AUTH_ERROR_MSG = "Authentication token is missing or invalid";
    private static final String UNHANDLED_ERROR_MSG = "Unhandled exception occurred while processing request";
    private static final String TYPE_MISMATCH_MSG = "Failed to convert value 'abc' to required type 'java.time.LocalDate'";
    private static final String REFRESH_TOKEN_ERROR_MSG = "Refresh token is invalid or expired";
    private static final String INVALID_CREDENTIALS_MSG = "Invalid username or password";
    private static final String BRUTE_FORCE_BLOCKED_MSG = "Too many failed login attempts, try again later";
    private static final String ACCESS_DENIED_MSG = "Access denied: insufficient permissions";
    private static final String MICROSERVICE_UNAVAILABLE_MSG = "External service is temporarily unavailable: Workload service is temporarily unavailable for processing trainer workload";
    private static final String JMS_ERROR_MSG = "Failed to send JMS message for trainer workload";

    @InjectMocks
    private ErrorHandler errorHandler;

    @ParameterizedTest
    @MethodSource("invalidRequestProvider")
    void handleServiceException_whenPrefixMatches_returnsInvalidRequestError(String errorMessage) {
        ServiceException ex = new ServiceException(errorMessage);
        String expectedMessage = format("%s%s", INVALID_REQUEST_ERROR.getMessage(), errorMessage);

        ResponseEntity<ErrorResponse> result = errorHandler.handleServiceException(ex);

        assertNotNull(result.getBody());
        assertEquals(BAD_REQUEST, result.getStatusCode());
        assertEquals(INVALID_REQUEST_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(expectedMessage, result.getBody().getErrorMessage());
    }

    @Test
    void handleServiceException_whenPrefixUnknown_returnsServerError() {
        ServiceException ex = new ServiceException(UNKNOWN_SERVICE_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleServiceException(ex);

        assertNotNull(result.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(SERVER_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(SERVER_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleDaoException_whenThrown_returnsDatabaseError() {
        DaoException ex = new DaoException(DAO_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleDaoException(ex);

        assertNotNull(result.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(DATABASE_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(DATABASE_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleEntityNotFoundException_whenThrown_returnsNotFoundErrorWithMessage() {
        EntityNotFoundException ex = new EntityNotFoundException(ENTITY_NOT_FOUND_MSG);
        String expectedMessage = format("%s%s", NOT_FOUND_ERROR.getMessage(), ENTITY_NOT_FOUND_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleEntityNotFoundException(ex);

        assertNotNull(result.getBody());
        assertEquals(NOT_FOUND, result.getStatusCode());
        assertEquals(NOT_FOUND_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(expectedMessage, result.getBody().getErrorMessage());
    }

    @Test
    void handleValidationException_whenMethodArgumentNotValid_returnsValidationError() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("objectName", VALIDATION_MSG)));

        Method method = this.getClass().getMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> result = errorHandler.handleValidationException(ex);

        assertNotNull(result.getBody());
        assertEquals(BAD_REQUEST, result.getStatusCode());
        assertEquals(VALIDATION_ERROR.getCode(), result.getBody().getErrorCode());
        assertTrue(result.getBody().getErrorMessage().contains(VALIDATION_MSG));
    }

    @Test
    void handleValidationException_whenThrown_returnsValidationErrorWithMessage() {
        ConstraintViolationException ex = new ConstraintViolationException(VALIDATION_MSG, null);
        String expectedMessage = format("%s%s", VALIDATION_ERROR.getMessage(), VALIDATION_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleValidationException(ex);

        assertNotNull(result.getBody());
        assertEquals(BAD_REQUEST, result.getStatusCode());
        assertEquals(VALIDATION_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(expectedMessage, result.getBody().getErrorMessage());
    }

    @Test
    void handleUserNotAuthenticatedException_whenThrown_returnsAuthenticationError() {
        UserNotAuthenticatedException ex = new UserNotAuthenticatedException(AUTH_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleUserNotAuthenticatedException(ex);

        assertNotNull(result.getBody());
        assertEquals(UNAUTHORIZED, result.getStatusCode());
        assertEquals(AUTHENTICATION_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(AUTHENTICATION_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleUserNotAuthenticatedException_whenThrown_returnsAuthorizedError() {
        UserNotAuthorizedException ex = new UserNotAuthorizedException(AUTH_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleUserNotAuthorizedException(ex);

        assertNotNull(result.getBody());
        assertEquals(UNAUTHORIZED, result.getStatusCode());
        assertEquals(AUTHORIZATION_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(AUTHORIZATION_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleUnhandledException_whenThrown_returnsServerError() {
        RuntimeException ex = new RuntimeException(UNHANDLED_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleUnhandledException(ex);

        assertNotNull(result.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(SERVER_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(SERVER_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleAuthorizationDeniedException_whenThrown_returnsForbiddenError() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException(ACCESS_DENIED_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleAuthorizationDeniedException(ex);

        assertNotNull(result.getBody());
        assertEquals(FORBIDDEN, result.getStatusCode());
        assertEquals(ACCESS_DENIED_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(ACCESS_DENIED_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleRefreshTokenNotFoundException_whenThrown_returnsTokenInvalidError() {
        RefreshTokenNotFoundException ex = new RefreshTokenNotFoundException(REFRESH_TOKEN_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleRefreshTokenNotFoundException(ex);

        assertNotNull(result.getBody());
        assertEquals(UNAUTHORIZED, result.getStatusCode());
        assertEquals(TOKEN_INVALID_ERROR.getCode(), result.getBody().getErrorCode());
        assertEquals(TOKEN_INVALID_ERROR.getMessage(), result.getBody().getErrorMessage());
    }

    @Test
    void handleUserBlockedException_whenThrown_returnsBruteForceBlockedError() {
        UserBlockedException ex = new UserBlockedException(BRUTE_FORCE_BLOCKED_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleUserBlockedException(ex);

        assertNotNull(result.getBody());
        assertEquals(BRUTE_FORCE_BLOCKED.getHttpStatus(), result.getStatusCode());
        assertEquals(BRUTE_FORCE_BLOCKED.getCode(), result.getBody().getErrorCode());
        assertTrue(result.getBody().getErrorMessage().contains(BRUTE_FORCE_BLOCKED_MSG));
    }

    @Test
    void handleInvalidCredentialsException_whenThrown_returnsInvalidCredentialsError() {
        InvalidCredentialsException ex = new InvalidCredentialsException(INVALID_CREDENTIALS_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleInvalidCredentialsException(ex);

        assertNotNull(result.getBody());
        assertEquals(INVALID_CREDENTIALS.getHttpStatus(), result.getStatusCode());
        assertEquals(INVALID_CREDENTIALS.getCode(), result.getBody().getErrorCode());
        assertTrue(result.getBody().getErrorMessage().contains(INVALID_CREDENTIALS_MSG));
    }

    @Test
    void handleTypeMismatchException_whenThrown_returnsInvalidRequestError() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", java.time.LocalDate.class, "periodFrom", null, new IllegalArgumentException(TYPE_MISMATCH_MSG));

        ResponseEntity<ErrorResponse> result = errorHandler.handleTypeMismatchException(ex);

        assertNotNull(result.getBody());
        assertEquals(INVALID_REQUEST_ERROR.getHttpStatus(), result.getStatusCode());
        assertEquals(INVALID_REQUEST_ERROR.getCode(), result.getBody().getErrorCode());

        String actualMessage = result.getBody().getErrorMessage();
        assertTrue(actualMessage.contains(INVALID_REQUEST_ERROR.getMessage()));
        assertTrue(actualMessage.contains("periodFrom"));
        assertTrue(actualMessage.contains(TYPE_MISMATCH_MSG));
    }

    @Test
    void handleMicroserviceUnavailableException_whenThrown_returnsMicroserviceUnavailableError() {
        MicroserviceUnavailableException ex = new MicroserviceUnavailableException(MICROSERVICE_UNAVAILABLE_MSG, null);

        ResponseEntity<ErrorResponse> result = errorHandler.handleMicroserviceUnavailable(ex);

        assertNotNull(result.getBody());
        assertEquals(ApiError.MICROSERVICE_UNAVAILABLE.getHttpStatus(), result.getStatusCode());
        assertEquals(ApiError.MICROSERVICE_UNAVAILABLE.getCode(), result.getBody().getErrorCode());
        assertTrue(result.getBody().getErrorMessage().contains(MICROSERVICE_UNAVAILABLE_MSG));
    }

    @Test
    void handleJmsMessageException_whenThrown_returnsJmsError() {
        JmsMessageException ex = new JmsMessageException(JMS_ERROR_MSG);

        ResponseEntity<ErrorResponse> result = errorHandler.handleJmsMessageException(ex);

        assertNotNull(result.getBody());
        assertEquals(ApiError.JMS_ERROR.getHttpStatus(), result.getStatusCode());
        assertEquals(ApiError.JMS_ERROR.getCode(), result.getBody().getErrorCode());
        assertTrue(result.getBody().getErrorMessage().contains(JMS_ERROR_MSG));
    }

    public void dummyMethod(String param) {
        throw new UnsupportedOperationException("This is a dummy method used only for test parameter reflection.");
    }

    private static Stream<Arguments> invalidRequestProvider() {
        return Stream.of("Invalid trainee username",
                        "Invalid trainer username",
                        "Trainee username must be provided",
                        "Trainer username must be provided",
                        "Username must not be null",
                        "Invalid training type")
                .map(Arguments::of);
    }
}