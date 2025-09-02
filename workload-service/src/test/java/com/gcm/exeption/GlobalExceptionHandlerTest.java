package com.gcm.exeption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithFieldErrors() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "username", "must not be blank");
        FieldError fieldError2 = new FieldError("object", "firstName", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", response.getBody().get("username"));
        assertEquals("must not be blank", response.getBody().get("firstName"));
    }

    @Test
    void handleResourceNotFound_shouldReturnNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Trainer not found");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainer not found", response.getBody().get("error"));
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");
        ResponseEntity<Map<String, String>> response = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().get("error"));
    }
}