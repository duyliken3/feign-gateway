package com.example.feigngateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceUnavailableException extends GatewayException {
    
    private final String serviceName;
    private final String reason;
    
    public ServiceUnavailableException(String serviceName, String reason) {
        super(String.format("Service '%s' is currently unavailable: %s", serviceName, reason), 
              HttpStatus.SERVICE_UNAVAILABLE);
        this.serviceName = serviceName;
        this.reason = reason;
    }
    
    public ServiceUnavailableException(String serviceName, String reason, Throwable cause) {
        super(String.format("Service '%s' is currently unavailable: %s", serviceName, reason), 
              HttpStatus.SERVICE_UNAVAILABLE, cause);
        this.serviceName = serviceName;
        this.reason = reason;
    }
}
