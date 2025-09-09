package com.example.feigngateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ValidationException extends GatewayException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message, HttpStatus.BAD_REQUEST);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String message, String validationError) {
        super(message, HttpStatus.BAD_REQUEST);
        this.validationErrors = List.of(validationError);
    }
    
    public ValidationException(String message, Throwable cause, List<String> validationErrors) {
        super(message, cause, HttpStatus.BAD_REQUEST);
        this.validationErrors = validationErrors;
    }
}
