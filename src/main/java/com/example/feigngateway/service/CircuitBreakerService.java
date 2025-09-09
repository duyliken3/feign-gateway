package com.example.feigngateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class CircuitBreakerService {
    
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    
    public enum CircuitState {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, requests are rejected
        HALF_OPEN  // Testing if service is back
    }
    
    public static class CircuitBreakerState {
        private volatile CircuitState state = CircuitState.CLOSED;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        
        // Configuration
        private final int failureThreshold = 5;
        private final long timeoutDuration = 60000; // 60 seconds
        private final int successThreshold = 3;
        
        public CircuitState getState() {
            return state;
        }
        
        public void recordSuccess() {
            if (state == CircuitState.HALF_OPEN) {
                int success = successCount.incrementAndGet();
                if (success >= successThreshold) {
                    state = CircuitState.CLOSED;
                    failureCount.set(0);
                    successCount.set(0);
                    log.info("Circuit breaker closed - service recovered");
                }
            } else if (state == CircuitState.CLOSED) {
                failureCount.set(0);
            }
        }
        
        public void recordFailure() {
            int failures = failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            
            if (state == CircuitState.CLOSED && failures >= failureThreshold) {
                state = CircuitState.OPEN;
                log.warn("Circuit breaker opened - too many failures: {}", failures);
            }
        }
        
        public boolean shouldAttemptRequest() {
            if (state == CircuitState.CLOSED) {
                return true;
            }
            
            if (state == CircuitState.OPEN) {
                long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
                if (timeSinceLastFailure >= timeoutDuration) {
                    state = CircuitState.HALF_OPEN;
                    successCount.set(0);
                    log.info("Circuit breaker moved to half-open state");
                    return true;
                }
                return false;
            }
            
            if (state == CircuitState.HALF_OPEN) {
                return true;
            }
            
            return false;
        }
    }
    
    public boolean isRequestAllowed(String serviceName) {
        CircuitBreakerState circuitBreaker = circuitBreakers.computeIfAbsent(serviceName, 
            k -> new CircuitBreakerState());
        
        return circuitBreaker.shouldAttemptRequest();
    }
    
    public void recordSuccess(String serviceName) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(serviceName);
        if (circuitBreaker != null) {
            circuitBreaker.recordSuccess();
        }
    }
    
    public void recordFailure(String serviceName) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(serviceName);
        if (circuitBreaker != null) {
            circuitBreaker.recordFailure();
        }
    }
    
    public CircuitState getCircuitState(String serviceName) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(serviceName);
        return circuitBreaker != null ? circuitBreaker.getState() : CircuitState.CLOSED;
    }
    
    public String getCircuitBreakerStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Circuit Breaker Statistics:\n");
        
        circuitBreakers.forEach((service, state) -> {
            stats.append(String.format("Service: %s, State: %s, Failures: %d, Successes: %d\n",
                service, state.getState(), state.failureCount.get(), state.successCount.get()));
        });
        
        return stats.toString();
    }
}
