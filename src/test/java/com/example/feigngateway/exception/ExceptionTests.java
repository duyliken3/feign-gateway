package com.example.feigngateway.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExceptionTests {

    @Test
    public void gatewayException_WithMessageAndStatusCode_ShouldSetCorrectly() {
        // Given
        String message = "Test error message";
        int statusCode = 404;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
        assertNull(exception.getCause());
    }

    @Test
    void gatewayException_WithMessageCauseAndStatusCode_ShouldSetCorrectly() {
        // Given
        String message = "Test error message";
        int statusCode = 500;
        Throwable cause = new RuntimeException("Root cause");

        // When
        GatewayException exception = new GatewayException(message, cause, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void gatewayException_WithNullMessage_ShouldHandleGracefully() {
        // Given
        String message = null;
        int statusCode = 400;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertNull(exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void gatewayException_WithZeroStatusCode_ShouldSetCorrectly() {
        // Given
        String message = "Test message";
        int statusCode = 0;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void gatewayException_WithNegativeStatusCode_ShouldSetCorrectly() {
        // Given
        String message = "Test message";
        int statusCode = -1;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void gatewayException_WithLargeStatusCode_ShouldSetCorrectly() {
        // Given
        String message = "Test message";
        int statusCode = 999;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void gatewayException_WithEmptyMessage_ShouldSetCorrectly() {
        // Given
        String message = "";
        int statusCode = 400;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void gatewayException_WithNullCause_ShouldHandleGracefully() {
        // Given
        String message = "Test message";
        int statusCode = 500;
        Throwable cause = null;

        // When
        GatewayException exception = new GatewayException(message, cause, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
        assertNull(exception.getCause());
    }

    @Test
    void gatewayException_GetStatusCode_ShouldReturnCorrectValue() {
        // Given
        String message = "Test message";
        int expectedStatusCode = 403;

        // When
        GatewayException exception = new GatewayException(message, expectedStatusCode);
        int actualStatusCode = exception.getStatusCode();

        // Then
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void gatewayException_GetMessage_ShouldReturnCorrectValue() {
        // Given
        String expectedMessage = "Access denied";
        int statusCode = 403;

        // When
        GatewayException exception = new GatewayException(expectedMessage, statusCode);
        String actualMessage = exception.getMessage();

        // Then
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void gatewayException_GetCause_ShouldReturnCorrectValue() {
        // Given
        String message = "Test message";
        int statusCode = 500;
        RuntimeException expectedCause = new RuntimeException("Root cause");

        // When
        GatewayException exception = new GatewayException(message, expectedCause, statusCode);
        Throwable actualCause = exception.getCause();

        // Then
        assertEquals(expectedCause, actualCause);
    }

    @Test
    void gatewayException_ShouldBeInstanceOfRuntimeException() {
        // Given
        String message = "Test message";
        int statusCode = 400;

        // When
        GatewayException exception = new GatewayException(message, statusCode);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void gatewayException_WithChainedExceptions_ShouldMaintainChain() {
        // Given
        String message = "Top level error";
        int statusCode = 500;
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);

        // When
        GatewayException exception = new GatewayException(message, intermediateCause, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
