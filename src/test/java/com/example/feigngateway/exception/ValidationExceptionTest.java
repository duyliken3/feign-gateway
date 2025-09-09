package com.example.feigngateway.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationException Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("Should create ValidationException with message and validation errors")
    void shouldCreateValidationExceptionWithMessageAndValidationErrors() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required", "Invalid format");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create ValidationException with message, cause and validation errors")
    void shouldCreateValidationExceptionWithMessageCauseAndValidationErrors() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required");
        Throwable cause = new IllegalArgumentException("Invalid input");

        // When
        ValidationException exception = new ValidationException(message, cause, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should handle null message gracefully")
    void shouldHandleNullMessageGracefully() {
        // Given
        String message = null;
        List<String> validationErrors = Arrays.asList("Field is required");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertNull(exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle empty message gracefully")
    void shouldHandleEmptyMessageGracefully() {
        // Given
        String message = "";
        List<String> validationErrors = Arrays.asList("Field is required");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals("", exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle null validation errors gracefully")
    void shouldHandleNullValidationErrorsGracefully() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = null;

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getValidationErrors());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle empty validation errors gracefully")
    void shouldHandleEmptyValidationErrorsGracefully() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Collections.emptyList();

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertTrue(exception.getValidationErrors().isEmpty());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle null cause gracefully")
    void shouldHandleNullCauseGracefully() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required");
        Throwable cause = null;

        // When
        ValidationException exception = new ValidationException(message, cause, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should maintain validation errors immutability")
    void shouldMaintainValidationErrorsImmutability() {
        // Given
        String message = "Validation failed";
        List<String> originalErrors = Arrays.asList("Field is required", "Invalid format");
        ValidationException exception = new ValidationException(message, originalErrors);

        // When
        List<String> retrievedErrors = exception.getValidationErrors();

        // Then
        assertEquals(originalErrors, retrievedErrors);
        assertNotSame(originalErrors, retrievedErrors); // Should be a copy
    }

    @Test
    @DisplayName("Should handle single validation error")
    void shouldHandleSingleValidationError() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(1, exception.getValidationErrors().size());
        assertEquals("Field is required", exception.getValidationErrors().get(0));
    }

    @Test
    @DisplayName("Should handle multiple validation errors")
    void shouldHandleMultipleValidationErrors() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList(
                "Field is required",
                "Invalid format",
                "Value too long",
                "Invalid characters"
        );

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(4, exception.getValidationErrors().size());
        assertTrue(exception.getValidationErrors().contains("Field is required"));
        assertTrue(exception.getValidationErrors().contains("Invalid format"));
        assertTrue(exception.getValidationErrors().contains("Value too long"));
        assertTrue(exception.getValidationErrors().contains("Invalid characters"));
    }

    @Test
    @DisplayName("Should handle chained exceptions with validation errors")
    void shouldHandleChainedExceptionsWithValidationErrors() {
        // Given
        String message = "Top level validation error";
        List<String> validationErrors = Arrays.asList("Field is required");
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalArgumentException intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);

        // When
        ValidationException exception = new ValidationException(message, intermediateCause, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(validationErrors, exception.getValidationErrors());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideMeaningfulToStringRepresentation() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList("Field is required", "Invalid format");

        // When
        ValidationException exception = new ValidationException(message, validationErrors);
        String toString = exception.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(ValidationException.class.getSimpleName()));
        assertTrue(toString.contains(message));
    }

    @Test
    @DisplayName("Should handle very long validation error messages")
    void shouldHandleVeryLongValidationErrorMessages() {
        // Given
        String message = "Validation failed";
        String longError = "This is a very long validation error message that might exceed normal limits and should be handled gracefully by the ValidationException class without causing any issues or performance problems";
        List<String> validationErrors = Arrays.asList(longError);

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(1, exception.getValidationErrors().size());
        assertEquals(longError, exception.getValidationErrors().get(0));
    }

    @Test
    @DisplayName("Should handle special characters in validation errors")
    void shouldHandleSpecialCharactersInValidationErrors() {
        // Given
        String message = "Validation failed";
        List<String> validationErrors = Arrays.asList(
                "Field contains invalid characters: @#$%",
                "Path must not contain: \\/:*?\"<>|",
                "Value contains unicode: 测试中文",
                "Value contains emoji: 🚀✨"
        );

        // When
        ValidationException exception = new ValidationException(message, validationErrors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(4, exception.getValidationErrors().size());
        assertTrue(exception.getValidationErrors().contains("Field contains invalid characters: @#$%"));
        assertTrue(exception.getValidationErrors().contains("Path must not contain: \\/:*?\"<>|"));
        assertTrue(exception.getValidationErrors().contains("Value contains unicode: 测试中文"));
        assertTrue(exception.getValidationErrors().contains("Value contains emoji: 🚀✨"));
    }
}
