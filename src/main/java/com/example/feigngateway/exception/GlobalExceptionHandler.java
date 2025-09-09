package com.example.feigngateway.exception;

import com.example.feigngateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ErrorResponse> handleGatewayException(GatewayException ex) {
        log.error("Gateway error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .statusCode(ex.getStatusCode())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(ex.getValidationErrors())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
        log.error("Service unavailable: {} - {}", ex.getServiceName(), ex.getReason());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .data(new ServiceErrorData(ex.getServiceName(), ex.getReason()))
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded for service: {}", ex.getServiceName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .statusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                .data(new RateLimitData(ex.getServiceName(), ex.getLimit(), ex.getRetryAfterSeconds()))
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.warn("HTTP client error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Downstream service error: " + ex.getMessage())
                .statusCode(ex.getStatusCode().value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
    
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Downstream service error: " + ex.getMessage())
                .statusCode(HttpStatus.BAD_GATEWAY.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Resource access error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Service temporarily unavailable: " + ex.getMessage())
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Method argument validation error: {}", ex.getMessage());
        
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.add(error.getField() + ": " + error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(error -> 
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Validation failed")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.warn("Bind exception: {}", ex.getMessage());
        
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.add(error.getField() + ": " + error.getDefaultMessage()));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Binding validation failed")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage()));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Constraint validation failed")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("HTTP method not supported: " + ex.getMethod())
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Missing required parameter: " + ex.getParameterName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Invalid parameter type for: " + ex.getName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HTTP message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Invalid request body format")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("Max upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("File size exceeds maximum allowed size")
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Internal server error. Error ID: " + errorId)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    // Helper classes for structured error data
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServiceErrorData {
        private String serviceName;
        private String reason;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RateLimitData {
        private String serviceName;
        private int limit;
        private long retryAfterSeconds;
    }
}
