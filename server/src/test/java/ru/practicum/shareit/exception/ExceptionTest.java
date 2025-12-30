package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void testCustomExceptions() {
        NotFoundException notFound = new NotFoundException("Not found");
        assertEquals("Not found", notFound.getMessage());

        ValidationException validation = new ValidationException("Invalid");
        assertEquals("Invalid", validation.getMessage());

        ConflictException conflict = new ConflictException("Conflict");
        assertEquals("Conflict", conflict.getMessage());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse response1 = new ErrorResponse("Just error");
        assertEquals("Just error", response1.getError());
        assertNull(response1.getDescription());

        ErrorResponse response2 = new ErrorResponse("Error", "Description");
        assertEquals("Error", response2.getError());
        assertEquals("Description", response2.getDescription());

        response2.setError("New Error");
        response2.setDescription("New Description");
        assertEquals("New Error", response2.getError());
        assertEquals("New Description", response2.getDescription());
    }

    @Test
    void testErrorHandlerMethods() {
        NotFoundException nfe = new NotFoundException("Not found msg");
        ErrorResponse resp1 = errorHandler.handleNotFoundException(nfe);
        assertEquals("Not found msg", resp1.getError());

        ValidationException ve = new ValidationException("Validation msg");
        ErrorResponse resp2 = errorHandler.handleValidationException(ve);
        assertEquals("Validation msg", resp2.getError());

        ConflictException ce = new ConflictException("Conflict msg");
        ErrorResponse resp3 = errorHandler.handleConflictException(ce);
        assertEquals("Conflict msg", resp3.getError());

        IllegalArgumentException iae = new IllegalArgumentException("Illegal arg");
        ErrorResponse resp4 = errorHandler.handleIllegalArgumentException(iae);
        assertEquals("Illegal arg", resp4.getError());

        Exception e = new Exception("General error");
        ErrorResponse resp5 = errorHandler.handleException(e);
        assertEquals("Internal server error", resp5.getError());
        assertEquals("General error", resp5.getDescription());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        BindingResult mockBindingResult = mock(BindingResult.class);
        FieldError mockFieldError = new FieldError("object", "field", "default message");

        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getFieldErrors()).thenReturn(List.of(mockFieldError));

        ErrorResponse response = errorHandler.handleMethodArgumentNotValidException(mockException);

        assertNotNull(response);
        assertEquals("field: default message", response.getError());
    }

    @Test
    void testHandleMethodArgumentNotValidException_NoErrors() {
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        BindingResult mockBindingResult = mock(BindingResult.class);

        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ErrorResponse response = errorHandler.handleMethodArgumentNotValidException(mockException);

        assertEquals("Validation error", response.getError());
    }
}