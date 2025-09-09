package com.example.feigngateway.controller;

import com.example.feigngateway.service.CacheService;
import com.example.feigngateway.service.CircuitBreakerService;
import com.example.feigngateway.service.PerformanceMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceController Tests")
class PerformanceControllerTest {

    @Mock
    private PerformanceMetricsService metricsService;

    @Mock
    private CircuitBreakerService circuitBreakerService;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PerformanceController performanceController;

    private static final String SERVICE_NAME = "test-service";

    @BeforeEach
    void setUp() {
        // Setup common mocks
        when(metricsService.getOverallStats()).thenReturn("Overall stats");
        when(circuitBreakerService.getCircuitBreakerStats()).thenReturn("Circuit breaker stats");
        when(cacheService.getCacheStats()).thenReturn(mock(CacheService.CacheStats.class));
    }

    @Test
    @DisplayName("Should return overall performance statistics")
    void shouldReturnOverallPerformanceStatistics() {
        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getOverallStats();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("overall"));
        assertTrue(body.containsKey("circuitBreakers"));
        assertTrue(body.containsKey("cacheStats"));

        verify(metricsService).getOverallStats();
        verify(circuitBreakerService).getCircuitBreakerStats();
        verify(cacheService).getCacheStats();
    }

    @Test
    @DisplayName("Should return service-specific performance statistics")
    void shouldReturnServiceSpecificPerformanceStatistics() {
        // Given
        when(metricsService.getServiceStats(SERVICE_NAME)).thenReturn("Service stats");
        when(circuitBreakerService.getCircuitState(SERVICE_NAME))
                .thenReturn(CircuitBreakerService.CircuitState.CLOSED);

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getServiceStats(SERVICE_NAME);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(SERVICE_NAME, body.get("service"));
        assertEquals("Service stats", body.get("metrics"));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, body.get("circuitState"));

        verify(metricsService).getServiceStats(SERVICE_NAME);
        verify(circuitBreakerService).getCircuitState(SERVICE_NAME);
    }

    @Test
    @DisplayName("Should return circuit breaker status")
    void shouldReturnCircuitBreakerStatus() {
        // When
        ResponseEntity<String> response = performanceController.getCircuitBreakerStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Circuit breaker stats", response.getBody());

        verify(circuitBreakerService).getCircuitBreakerStats();
    }

    @Test
    @DisplayName("Should return cache statistics")
    void shouldReturnCacheStatistics() {
        // Given
        CacheService.CacheStats mockStats = mock(CacheService.CacheStats.class);
        when(cacheService.getCacheStats()).thenReturn(mockStats);

        // When
        ResponseEntity<CacheService.CacheStats> response = performanceController.getCacheStats();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockStats, response.getBody());

        verify(cacheService).getCacheStats();
    }

    @Test
    @DisplayName("Should return performance health status")
    void shouldReturnPerformanceHealthStatus() {
        // Given
        when(metricsService.getStartTime()).thenReturn(mock(java.util.concurrent.atomic.AtomicLong.class));
        when(metricsService.getTotalRequests()).thenReturn(mock(java.util.concurrent.atomic.LongAdder.class));
        when(metricsService.getTotalErrors()).thenReturn(mock(java.util.concurrent.atomic.LongAdder.class));

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getPerformanceHealth();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("uptime"));
        assertTrue(response.getBody().containsKey("totalRequests"));
        assertTrue(response.getBody().containsKey("totalErrors"));

        verify(metricsService).getStartTime();
        verify(metricsService).getTotalRequests();
        verify(metricsService).getTotalErrors();
    }

    @Test
    @DisplayName("Should handle null service name gracefully")
    void shouldHandleNullServiceNameGracefully() {
        // Given
        when(metricsService.getServiceStats(null)).thenReturn("Service stats");
        when(circuitBreakerService.getCircuitState(null))
                .thenReturn(CircuitBreakerService.CircuitState.CLOSED);

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getServiceStats(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertNull(body.get("service"));

        verify(metricsService).getServiceStats(null);
        verify(circuitBreakerService).getCircuitState(null);
    }

    @Test
    @DisplayName("Should handle empty service name gracefully")
    void shouldHandleEmptyServiceNameGracefully() {
        // Given
        String emptyServiceName = "";
        when(metricsService.getServiceStats(emptyServiceName)).thenReturn("Service stats");
        when(circuitBreakerService.getCircuitState(emptyServiceName))
                .thenReturn(CircuitBreakerService.CircuitState.CLOSED);

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getServiceStats(emptyServiceName);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(emptyServiceName, body.get("service"));

        verify(metricsService).getServiceStats(emptyServiceName);
        verify(circuitBreakerService).getCircuitState(emptyServiceName);
    }

    @Test
    @DisplayName("Should handle service metrics service exceptions gracefully")
    void shouldHandleServiceMetricsServiceExceptionsGracefully() {
        // Given
        when(metricsService.getServiceStats(SERVICE_NAME))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                performanceController.getServiceStats(SERVICE_NAME));

        verify(metricsService).getServiceStats(SERVICE_NAME);
    }

    @Test
    @DisplayName("Should handle circuit breaker service exceptions gracefully")
    void shouldHandleCircuitBreakerServiceExceptionsGracefully() {
        // Given
        when(circuitBreakerService.getCircuitBreakerStats())
                .thenThrow(new RuntimeException("Circuit breaker unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                performanceController.getCircuitBreakerStatus());

        verify(circuitBreakerService).getCircuitBreakerStats();
    }

    @Test
    @DisplayName("Should handle cache service exceptions gracefully")
    void shouldHandleCacheServiceExceptionsGracefully() {
        // Given
        when(cacheService.getCacheStats())
                .thenThrow(new RuntimeException("Cache service unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                performanceController.getCacheStats());

        verify(cacheService).getCacheStats();
    }

    @Test
    @DisplayName("Should return consistent response format for overall stats")
    void shouldReturnConsistentResponseFormatForOverallStats() {
        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getOverallStats();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.size());
        assertTrue(body.containsKey("overall"));
        assertTrue(body.containsKey("circuitBreakers"));
        assertTrue(body.containsKey("cacheStats"));
    }

    @Test
    @DisplayName("Should return consistent response format for service stats")
    void shouldReturnConsistentResponseFormatForServiceStats() {
        // Given
        when(metricsService.getServiceStats(SERVICE_NAME)).thenReturn("Service stats");
        when(circuitBreakerService.getCircuitState(SERVICE_NAME))
                .thenReturn(CircuitBreakerService.CircuitState.OPEN);

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getServiceStats(SERVICE_NAME);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.size());
        assertTrue(body.containsKey("service"));
        assertTrue(body.containsKey("metrics"));
        assertTrue(body.containsKey("circuitState"));
    }

    @Test
    @DisplayName("Should return consistent response format for health status")
    void shouldReturnConsistentResponseFormatForHealthStatus() {
        // Given
        when(metricsService.getStartTime()).thenReturn(mock(java.util.concurrent.atomic.AtomicLong.class));
        when(metricsService.getTotalRequests()).thenReturn(mock(java.util.concurrent.atomic.LongAdder.class));
        when(metricsService.getTotalErrors()).thenReturn(mock(java.util.concurrent.atomic.LongAdder.class));

        // When
        ResponseEntity<Map<String, Object>> response = performanceController.getPerformanceHealth();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(5, body.size());
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("uptime"));
        assertTrue(body.containsKey("totalRequests"));
        assertTrue(body.containsKey("totalErrors"));
    }
}
