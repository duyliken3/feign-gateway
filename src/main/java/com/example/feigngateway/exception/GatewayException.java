package com.example.feigngateway.exception;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {
    
    private final int statusCode;
    
    public GatewayException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public GatewayException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
