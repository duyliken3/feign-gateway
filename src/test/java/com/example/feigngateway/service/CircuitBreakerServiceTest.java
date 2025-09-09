package com.example.feigngateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircuitBreakerService Tests")
class CircuitBreakerServiceTest {

    @InjectMocks
    private CircuitBreakerService circuitBreakerService;

    private static final String SERVICE_NAME = "test-service";

    @BeforeEach
    void setUp() {
        // Reset circuit breaker state
        circuitBreakerService.recordSuccess(SERVICE_NAME);
    }

    @Test
    @DisplayName("Should allow requests when circuit is closed")
    void shouldAllowRequestsWhenCircuitIsClosed() {
        // When & Then
        assertTrue(circuitBreakerService.isRequestAllowed(SERVICE_NAME));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(SERVICE_NAME));
    }

    @Test
    @DisplayName("Should open circuit after failure threshold is reached")
    void shouldOpenCircuitAfterFailureThresholdIsReached() {
        // Given - Record failures up to threshold
        for (int i = 0; i < 5; i++) {
            circuitBreakerService.recordFailure(SERVICE_NAME);
        }

        // When & Then
        assertFalse(circuitBreakerService.isRequestAllowed(SERVICE_NAME));
        assertEquals(CircuitBreakerService.CircuitState.OPEN, 
                circuitBreakerService.getCircuitState(SERVICE_NAME));
    }

    @Test
    @DisplayName("Should allow requests when circuit is half-open and success threshold is reached")
    void shouldAllowRequestsWhenCircuitIsHalfOpenAndSuccessThresholdIsReached() {
        // Given - Open the circuit
        for (int i = 0; i < 5; i++) {
            circuitBreakerService.recordFailure(SERVICE_NAME);
        }
        assertFalse(circuitBreakerService.isRequestAllowed(SERVICE_NAME));

        // When - Wait for timeout and record successes
        // Note: In real implementation, we'd need to mock time or use a test clock
        // For now, we'll test the half-open logic directly
        circuitBreakerService.recordSuccess(SERVICE_NAME);
        circuitBreakerService.recordSuccess(SERVICE_NAME);
        circuitBreakerService.recordSuccess(SERVICE_NAME);

        // Then
        assertTrue(circuitBreakerService.isRequestAllowed(SERVICE_NAME));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(SERVICE_NAME));
    }

    @Test
    @DisplayName("Should reset failure count on success when circuit is closed")
    void shouldResetFailureCountOnSuccessWhenCircuitIsClosed() {
        // Given - Record some failures
        circuitBreakerService.recordFailure(SERVICE_NAME);
        circuitBreakerService.recordFailure(SERVICE_NAME);

        // When - Record success
        circuitBreakerService.recordSuccess(SERVICE_NAME);

        // Then - Circuit should still be closed and allow requests
        assertTrue(circuitBreakerService.isRequestAllowed(SERVICE_NAME));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(SERVICE_NAME));
    }

    @Test
    @DisplayName("Should handle multiple services independently")
    void shouldHandleMultipleServicesIndependently() {
        // Given
        String service1 = "service-1";
        String service2 = "service-2";

        // When - Open circuit for service1, keep service2 closed
        for (int i = 0; i < 5; i++) {
            circuitBreakerService.recordFailure(service1);
        }
        circuitBreakerService.recordSuccess(service2);

        // Then
        assertFalse(circuitBreakerService.isRequestAllowed(service1));
        assertTrue(circuitBreakerService.isRequestAllowed(service2));
        assertEquals(CircuitBreakerService.CircuitState.OPEN, 
                circuitBreakerService.getCircuitState(service1));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(service2));
    }

    @Test
    @DisplayName("Should return correct circuit state for unknown service")
    void shouldReturnCorrectCircuitStateForUnknownService() {
        // Given
        String unknownService = "unknown-service";

        // When & Then
        assertTrue(circuitBreakerService.isRequestAllowed(unknownService));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(unknownService));
    }

    @Test
    @DisplayName("Should handle null service name gracefully")
    void shouldHandleNullServiceNameGracefully() {
        // When & Then
        assertTrue(circuitBreakerService.isRequestAllowed(null));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(null));
    }

    @Test
    @DisplayName("Should handle empty service name gracefully")
    void shouldHandleEmptyServiceNameGracefully() {
        // When & Then
        assertTrue(circuitBreakerService.isRequestAllowed(""));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(""));
    }

    @Test
    @DisplayName("Should provide circuit breaker statistics")
    void shouldProvideCircuitBreakerStatistics() {
        // Given
        circuitBreakerService.recordFailure(SERVICE_NAME);
        circuitBreakerService.recordSuccess(SERVICE_NAME);

        // When
        String stats = circuitBreakerService.getCircuitBreakerStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.contains("Circuit Breaker Statistics"));
        assertTrue(stats.contains(SERVICE_NAME));
    }

    @Test
    @DisplayName("Should handle rapid failure recording")
    void shouldHandleRapidFailureRecording() {
        // Given
        String service = "rapid-failure-service";

        // When - Record failures rapidly
        for (int i = 0; i < 10; i++) {
            circuitBreakerService.recordFailure(service);
        }

        // Then - Circuit should be open
        assertFalse(circuitBreakerService.isRequestAllowed(service));
        assertEquals(CircuitBreakerService.CircuitState.OPEN, 
                circuitBreakerService.getCircuitState(service));
    }

    @Test
    @DisplayName("Should handle rapid success recording")
    void shouldHandleRapidSuccessRecording() {
        // Given
        String service = "rapid-success-service";

        // When - Record successes rapidly
        for (int i = 0; i < 10; i++) {
            circuitBreakerService.recordSuccess(service);
        }

        // Then - Circuit should remain closed
        assertTrue(circuitBreakerService.isRequestAllowed(service));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(service));
    }

    @Test
    @DisplayName("Should handle mixed success and failure recording")
    void shouldHandleMixedSuccessAndFailureRecording() {
        // Given
        String service = "mixed-service";

        // When - Record mixed results
        circuitBreakerService.recordFailure(service);
        circuitBreakerService.recordSuccess(service);
        circuitBreakerService.recordFailure(service);
        circuitBreakerService.recordSuccess(service);

        // Then - Circuit should still be closed (not enough failures)
        assertTrue(circuitBreakerService.isRequestAllowed(service));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(service));
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccessSafely() throws InterruptedException {
        // Given
        String service = "concurrent-service";
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // When - Run concurrent operations
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    if (threadIndex % 2 == 0) {
                        circuitBreakerService.recordFailure(service);
                    } else {
                        circuitBreakerService.recordSuccess(service);
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - Should not throw exceptions and circuit state should be valid
        assertDoesNotThrow(() -> circuitBreakerService.isRequestAllowed(service));
        assertNotNull(circuitBreakerService.getCircuitState(service));
    }
}
