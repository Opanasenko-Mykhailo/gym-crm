package com.gcm.exeption;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String MUST_NOT_BE_BLANK = "must not be blank";
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String ERROR_KEY = "error";
    private static final String TRAINER_NOT_FOUND = "Trainer not found";
    private static final String SOMETHING_WENT_WRONG = "Something went wrong";
    private static final String FIELD_TRAINING_DATE = "trainingDate";
    private static final String ERROR_MESSAGE = "Training date cannot be in the past";

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void givenInvalidFields_whenHandleValidationExceptions_thenReturnsBadRequestWithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", USERNAME, MUST_NOT_BE_BLANK);
        FieldError fieldError2 = new FieldError("object", FIRST_NAME, MUST_NOT_BE_BLANK);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MUST_NOT_BE_BLANK, response.getBody().get(USERNAME));
        assertEquals(MUST_NOT_BE_BLANK, response.getBody().get(FIRST_NAME));
    }

    @Test
    void givenConstraintViolation_whenHandleConstraintViolation_thenReturnsBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getPropertyPath().toString()).thenReturn(FIELD_TRAINING_DATE);
        when(violation.getMessage()).thenReturn(ERROR_MESSAGE);

        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ERROR_MESSAGE, response.getBody().get(FIELD_TRAINING_DATE));
    }


    @Test
    void givenResourceNotFound_whenHandleResourceNotFound_thenReturnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException(TRAINER_NOT_FOUND);

        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(TRAINER_NOT_FOUND, response.getBody().get(ERROR_KEY));
    }

    @Test
    void givenUnhandledException_whenHandleAllExceptions_thenReturnsInternalServerError() {
        Exception ex = new RuntimeException(SOMETHING_WENT_WRONG);

        ResponseEntity<Map<String, String>> response = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(SOMETHING_WENT_WRONG, response.getBody().get(ERROR_KEY));
    }
}