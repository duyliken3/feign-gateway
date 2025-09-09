package com.example.feigngateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitExceededException extends GatewayException {
    
    private final String serviceName;
    private final int limit;
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String serviceName, int limit, long retryAfterSeconds) {
        super(String.format("Rate limit exceeded for service '%s'. Limit: %d requests, retry after: %d seconds", 
                           serviceName, limit, retryAfterSeconds), 
              HttpStatus.TOO_MANY_REQUESTS);
        this.serviceName = serviceName;
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
