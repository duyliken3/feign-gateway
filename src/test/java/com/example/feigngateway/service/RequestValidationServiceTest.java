package com.example.feigngateway.service;

import com.example.feigngateway.dto.GatewayRequest;
import com.example.feigngateway.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestValidationService Tests")
class RequestValidationServiceTest {

    @InjectMocks
    private RequestValidationService validationService;

    private GatewayRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = GatewayRequest.builder()
                .service("user-service")
                .path("/users/1")
                .method("GET")
                .queryParams(Map.of("page", "1", "size", "10"))
                .headers(Map.of("Content-Type", "application/json"))
                .build();
    }

    @Test
    @DisplayName("Should validate valid gateway request successfully")
    void shouldValidateValidGatewayRequestSuccessfully() {
        // When & Then
        assertDoesNotThrow(() -> validationService.validateGatewayRequest(validRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when service name is null")
    void shouldThrowValidationExceptionWhenServiceNameIsNull() {
        // Given
        validRequest.setService(null);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertEquals("Request validation failed", exception.getMessage());
        assertTrue(exception.getValidationErrors().contains("Service name is required"));
    }

    @Test
    @DisplayName("Should throw ValidationException when service name is empty")
    void shouldThrowValidationExceptionWhenServiceNameIsEmpty() {
        // Given
        validRequest.setService("");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("Service name is required"));
    }

    @Test
    @DisplayName("Should throw ValidationException when service name contains invalid characters")
    void shouldThrowValidationExceptionWhenServiceNameContainsInvalidCharacters() {
        // Given
        validRequest.setService("user@service");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Service name must contain only alphanumeric characters, hyphens, and underscores"));
    }

    @Test
    @DisplayName("Should throw ValidationException when service name exceeds maximum length")
    void shouldThrowValidationExceptionWhenServiceNameExceedsMaximumLength() {
        // Given
        validRequest.setService("a".repeat(51));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Service name must not exceed 50 characters"));
    }

    @Test
    @DisplayName("Should throw ValidationException when path is null")
    void shouldThrowValidationExceptionWhenPathIsNull() {
        // Given
        validRequest.setPath(null);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("Path is required"));
    }

    @Test
    @DisplayName("Should throw ValidationException when path is empty")
    void shouldThrowValidationExceptionWhenPathIsEmpty() {
        // Given
        validRequest.setPath("");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("Path is required"));
    }

    @Test
    @DisplayName("Should throw ValidationException when path doesn't start with slash")
    void shouldThrowValidationExceptionWhenPathDoesntStartWithSlash() {
        // Given
        validRequest.setPath("users/1");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Path must start with / and contain only valid path characters"));
    }

    @Test
    @DisplayName("Should throw ValidationException when path exceeds maximum length")
    void shouldThrowValidationExceptionWhenPathExceedsMaximumLength() {
        // Given
        validRequest.setPath("/" + "a".repeat(500));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Path must not exceed 500 characters"));
    }

    @Test
    @DisplayName("Should throw ValidationException when HTTP method is null")
    void shouldThrowValidationExceptionWhenHttpMethodIsNull() {
        // Given
        validRequest.setMethod(null);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("HTTP method is required"));
    }

    @Test
    @DisplayName("Should throw ValidationException when HTTP method is invalid")
    void shouldThrowValidationExceptionWhenHttpMethodIsInvalid() {
        // Given
        validRequest.setMethod("INVALID");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "HTTP method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS"));
    }

    @Test
    @DisplayName("Should validate valid HTTP methods")
    void shouldValidateValidHttpMethods() {
        String[] validMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        
        for (String method : validMethods) {
            validRequest.setMethod(method);
            assertDoesNotThrow(() -> validationService.validateHttpMethod(method));
        }
    }

    @Test
    @DisplayName("Should throw ValidationException when too many query parameters")
    void shouldThrowValidationExceptionWhenTooManyQueryParams() {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        for (int i = 0; i < 101; i++) {
            queryParams.put("param" + i, "value" + i);
        }
        validRequest.setQueryParams(queryParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Too many query parameters. Maximum allowed: 100"));
    }

    @Test
    @DisplayName("Should throw ValidationException when query parameter key is empty")
    void shouldThrowValidationExceptionWhenQueryParamKeyIsEmpty() {
        // Given
        validRequest.setQueryParams(Map.of("", "value"));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("Query parameter key cannot be empty"));
    }

    @Test
    @DisplayName("Should throw ValidationException when query parameter key exceeds maximum length")
    void shouldThrowValidationExceptionWhenQueryParamKeyExceedsMaximumLength() {
        // Given
        validRequest.setQueryParams(Map.of("a".repeat(101), "value"));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Query parameter key 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' exceeds maximum length of 100 characters"));
    }

    @Test
    @DisplayName("Should throw ValidationException when query parameter value exceeds maximum length")
    void shouldThrowValidationExceptionWhenQueryParamValueExceedsMaximumLength() {
        // Given
        validRequest.setQueryParams(Map.of("param", "a".repeat(1001)));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Query parameter value for 'param' exceeds maximum length of 1000 characters"));
    }

    @Test
    @DisplayName("Should throw ValidationException when header name is empty")
    void shouldThrowValidationExceptionWhenHeaderNameIsEmpty() {
        // Given
        validRequest.setHeaders(Map.of("", "value"));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains("Header name cannot be empty"));
    }

    @Test
    @DisplayName("Should throw ValidationException when header value exceeds maximum size")
    void shouldThrowValidationExceptionWhenHeaderValueExceedsMaximumSize() {
        // Given
        validRequest.setHeaders(Map.of("header", "a".repeat(8193)));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        assertTrue(exception.getValidationErrors().contains(
                "Header value for 'header' exceeds maximum size of 8192 bytes"));
    }

    @Test
    @DisplayName("Should validate service name correctly")
    void shouldValidateServiceNameCorrectly() {
        // Valid service names
        String[] validNames = {"user-service", "api-gateway", "service123", "my_service"};
        for (String name : validNames) {
            assertTrue(validationService.isValidServiceName(name));
        }

        // Invalid service names
        String[] invalidNames = {"user@service", "service name", "service.name", null, ""};
        for (String name : invalidNames) {
            assertFalse(validationService.isValidServiceName(name));
        }
    }

    @Test
    @DisplayName("Should validate path correctly")
    void shouldValidatePathCorrectly() {
        // Valid paths
        String[] validPaths = {"/users", "/users/1", "/api/v1/data", "/path-with-dashes"};
        for (String path : validPaths) {
            assertTrue(validationService.isValidPath(path));
        }

        // Invalid paths
        String[] invalidPaths = {"users", "users/1", "/path with spaces", null, ""};
        for (String path : invalidPaths) {
            assertFalse(validationService.isValidPath(path));
        }
    }

    @Test
    @DisplayName("Should validate HTTP method correctly")
    void shouldValidateHttpMethodCorrectly() {
        // Valid methods
        String[] validMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        for (String method : validMethods) {
            assertTrue(validationService.isValidHttpMethod(method));
        }

        // Invalid methods
        String[] invalidMethods = {"INVALID", "get", "POST ", null, ""};
        for (String method : invalidMethods) {
            assertFalse(validationService.isValidHttpMethod(method));
        }
    }

    @Test
    @DisplayName("Should validate URL correctly")
    void shouldValidateUrlCorrectly() {
        // Valid URLs
        String[] validUrls = {
                "https://api.example.com",
                "http://localhost:8080",
                "https://api.example.com/path",
                "http://192.168.1.1:3000/api"
        };
        for (String url : validUrls) {
            assertTrue(validationService.isValidUrl(url));
        }

        // Invalid URLs
        String[] invalidUrls = {
                "ftp://api.example.com",
                "api.example.com",
                "https://",
                null,
                ""
        };
        for (String url : invalidUrls) {
            assertFalse(validationService.isValidUrl(url));
        }
    }

    @Test
    @DisplayName("Should handle multiple validation errors")
    void shouldHandleMultipleValidationErrors() {
        // Given
        validRequest.setService("invalid@service");
        validRequest.setPath("invalid-path");
        validRequest.setMethod("INVALID");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateGatewayRequest(validRequest));
        
        assertEquals("Request validation failed", exception.getMessage());
        assertTrue(exception.getValidationErrors().size() >= 3);
        assertTrue(exception.getValidationErrors().contains(
                "Service name must contain only alphanumeric characters, hyphens, and underscores"));
        assertTrue(exception.getValidationErrors().contains(
                "Path must start with / and contain only valid path characters"));
        assertTrue(exception.getValidationErrors().contains(
                "HTTP method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS"));
    }
}
