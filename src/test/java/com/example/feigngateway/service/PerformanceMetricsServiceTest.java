package com.example.feigngateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceMetricsService Tests")
class PerformanceMetricsServiceTest {

    @InjectMocks
    private PerformanceMetricsService metricsService;

    private static final String SERVICE_NAME = "test-service";

    @BeforeEach
    void setUp() {
        // Reset metrics for each test
        metricsService.resetMetrics();
    }

    @Test
    @DisplayName("Should record request metrics correctly")
    void shouldRecordRequestMetricsCorrectly() {
        // Given
        long responseTime = 150L;
        long bytesTransferred = 1024L;

        // When
        metricsService.recordRequest(SERVICE_NAME, responseTime, bytesTransferred);

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(1, serviceMetrics.getRequestCount());
        assertEquals(0, serviceMetrics.getErrorCount());
        assertEquals(responseTime, serviceMetrics.getAverageResponseTime());
        assertEquals(responseTime, serviceMetrics.getMinResponseTime());
        assertEquals(responseTime, serviceMetrics.getMaxResponseTime());
        assertEquals(bytesTransferred, serviceMetrics.getTotalBytesTransferred());
        assertEquals(0.0, serviceMetrics.getErrorRate());
    }

    @Test
    @DisplayName("Should record error metrics correctly")
    void shouldRecordErrorMetricsCorrectly() {
        // Given
        metricsService.recordRequest(SERVICE_NAME, 100L, 512L);
        metricsService.recordRequest(SERVICE_NAME, 200L, 1024L);

        // When
        metricsService.recordError(SERVICE_NAME);

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(2, serviceMetrics.getRequestCount());
        assertEquals(1, serviceMetrics.getErrorCount());
        assertEquals(50.0, serviceMetrics.getErrorRate());
    }

    @Test
    @DisplayName("Should calculate average response time correctly")
    void shouldCalculateAverageResponseTimeCorrectly() {
        // Given
        long[] responseTimes = {100L, 200L, 300L, 400L, 500L};
        long expectedAverage = 300L;

        // When
        for (long responseTime : responseTimes) {
            metricsService.recordRequest(SERVICE_NAME, responseTime, 1024L);
        }

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(responseTimes.length, serviceMetrics.getRequestCount());
        assertEquals(expectedAverage, serviceMetrics.getAverageResponseTime());
    }

    @Test
    @DisplayName("Should track min and max response times correctly")
    void shouldTrackMinAndMaxResponseTimesCorrectly() {
        // Given
        long[] responseTimes = {500L, 100L, 300L, 200L, 400L};
        long expectedMin = 100L;
        long expectedMax = 500L;

        // When
        for (long responseTime : responseTimes) {
            metricsService.recordRequest(SERVICE_NAME, responseTime, 1024L);
        }

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(expectedMin, serviceMetrics.getMinResponseTime());
        assertEquals(expectedMax, serviceMetrics.getMaxResponseTime());
    }

    @Test
    @DisplayName("Should calculate error rate correctly")
    void shouldCalculateErrorRateCorrectly() {
        // Given
        int totalRequests = 10;
        int errors = 3;
        double expectedErrorRate = 30.0;

        // When
        for (int i = 0; i < totalRequests; i++) {
            metricsService.recordRequest(SERVICE_NAME, 100L, 1024L);
        }
        for (int i = 0; i < errors; i++) {
            metricsService.recordError(SERVICE_NAME);
        }

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(totalRequests, serviceMetrics.getRequestCount());
        assertEquals(errors, serviceMetrics.getErrorCount());
        assertEquals(expectedErrorRate, serviceMetrics.getErrorRate());
    }

    @Test
    @DisplayName("Should handle zero requests gracefully")
    void shouldHandleZeroRequestsGracefully() {
        // When
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);

        // Then
        assertEquals(0, serviceMetrics.getRequestCount());
        assertEquals(0, serviceMetrics.getErrorCount());
        assertEquals(0.0, serviceMetrics.getAverageResponseTime());
        assertEquals(0, serviceMetrics.getMinResponseTime());
        assertEquals(0, serviceMetrics.getMaxResponseTime());
        assertEquals(0, serviceMetrics.getTotalBytesTransferred());
        assertEquals(0.0, serviceMetrics.getErrorRate());
    }

    @Test
    @DisplayName("Should handle multiple services independently")
    void shouldHandleMultipleServicesIndependently() {
        // Given
        String service1 = "service-1";
        String service2 = "service-2";

        // When
        metricsService.recordRequest(service1, 100L, 512L);
        metricsService.recordRequest(service1, 200L, 1024L);
        metricsService.recordError(service1);

        metricsService.recordRequest(service2, 300L, 2048L);
        metricsService.recordRequest(service2, 400L, 4096L);

        // Then
        var service1Metrics = metricsService.getServiceMetrics(service1);
        var service2Metrics = metricsService.getServiceMetrics(service2);

        assertEquals(2, service1Metrics.getRequestCount());
        assertEquals(1, service1Metrics.getErrorCount());
        assertEquals(150.0, service1Metrics.getAverageResponseTime());
        assertEquals(50.0, service1Metrics.getErrorRate());

        assertEquals(2, service2Metrics.getRequestCount());
        assertEquals(0, service2Metrics.getErrorCount());
        assertEquals(350.0, service2Metrics.getAverageResponseTime());
        assertEquals(0.0, service2Metrics.getErrorRate());
    }

    @Test
    @DisplayName("Should provide overall statistics")
    void shouldProvideOverallStatistics() {
        // Given
        metricsService.recordRequest(SERVICE_NAME, 100L, 1024L);
        metricsService.recordRequest(SERVICE_NAME, 200L, 2048L);
        metricsService.recordError(SERVICE_NAME);

        // When
        String stats = metricsService.getOverallStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.contains("Gateway Performance Statistics"));
        assertTrue(stats.contains("Total Requests: 2"));
        assertTrue(stats.contains("Total Errors: 1"));
        assertTrue(stats.contains("Error Rate: 50.0%"));
    }

    @Test
    @DisplayName("Should provide service-specific statistics")
    void shouldProvideServiceSpecificStatistics() {
        // Given
        metricsService.recordRequest(SERVICE_NAME, 150L, 1024L);
        metricsService.recordRequest(SERVICE_NAME, 250L, 2048L);
        metricsService.recordError(SERVICE_NAME);

        // When
        String stats = metricsService.getServiceStats(SERVICE_NAME);

        // Then
        assertNotNull(stats);
        assertTrue(stats.contains("Service: " + SERVICE_NAME));
        assertTrue(stats.contains("Requests: 2"));
        assertTrue(stats.contains("Errors: 1"));
        assertTrue(stats.contains("Error Rate: 50.0%"));
        assertTrue(stats.contains("Avg Response Time: 200.0 ms"));
    }

    @Test
    @DisplayName("Should reset metrics correctly")
    void shouldResetMetricsCorrectly() {
        // Given
        metricsService.recordRequest(SERVICE_NAME, 100L, 1024L);
        metricsService.recordError(SERVICE_NAME);

        // When
        metricsService.resetMetrics();

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(0, serviceMetrics.getRequestCount());
        assertEquals(0, serviceMetrics.getErrorCount());
        assertEquals(0.0, serviceMetrics.getAverageResponseTime());
        assertEquals(0, serviceMetrics.getMinResponseTime());
        assertEquals(0, serviceMetrics.getMaxResponseTime());
        assertEquals(0, serviceMetrics.getTotalBytesTransferred());
        assertEquals(0.0, serviceMetrics.getErrorRate());
    }

    @Test
    @DisplayName("Should handle null service name gracefully")
    void shouldHandleNullServiceNameGracefully() {
        // When & Then
        assertDoesNotThrow(() -> {
            metricsService.recordRequest(null, 100L, 1024L);
            metricsService.recordError(null);
        });

        var serviceMetrics = metricsService.getServiceMetrics(null);
        assertNotNull(serviceMetrics);
    }

    @Test
    @DisplayName("Should handle empty service name gracefully")
    void shouldHandleEmptyServiceNameGracefully() {
        // When & Then
        assertDoesNotThrow(() -> {
            metricsService.recordRequest("", 100L, 1024L);
            metricsService.recordError("");
        });

        var serviceMetrics = metricsService.getServiceMetrics("");
        assertNotNull(serviceMetrics);
    }

    @Test
    @DisplayName("Should handle negative response times")
    void shouldHandleNegativeResponseTimes() {
        // When
        metricsService.recordRequest(SERVICE_NAME, -100L, 1024L);

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(1, serviceMetrics.getRequestCount());
        assertEquals(-100.0, serviceMetrics.getAverageResponseTime());
        assertEquals(-100L, serviceMetrics.getMinResponseTime());
        assertEquals(-100L, serviceMetrics.getMaxResponseTime());
    }

    @Test
    @DisplayName("Should handle zero response times")
    void shouldHandleZeroResponseTimes() {
        // When
        metricsService.recordRequest(SERVICE_NAME, 0L, 1024L);

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(1, serviceMetrics.getRequestCount());
        assertEquals(0.0, serviceMetrics.getAverageResponseTime());
        assertEquals(0L, serviceMetrics.getMinResponseTime());
        assertEquals(0L, serviceMetrics.getMaxResponseTime());
    }

    @Test
    @DisplayName("Should handle very large response times")
    void shouldHandleVeryLargeResponseTimes() {
        // Given
        long largeResponseTime = Long.MAX_VALUE;

        // When
        metricsService.recordRequest(SERVICE_NAME, largeResponseTime, 1024L);

        // Then
        var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
        assertEquals(1, serviceMetrics.getRequestCount());
        assertEquals((double) largeResponseTime, serviceMetrics.getAverageResponseTime());
        assertEquals(largeResponseTime, serviceMetrics.getMinResponseTime());
        assertEquals(largeResponseTime, serviceMetrics.getMaxResponseTime());
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccessSafely() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // When - Run concurrent operations
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    if (threadIndex % 2 == 0) {
                        metricsService.recordRequest(SERVICE_NAME, 100L, 1024L);
                    } else {
                        metricsService.recordError(SERVICE_NAME);
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - Should not throw exceptions and metrics should be valid
        assertDoesNotThrow(() -> {
            var serviceMetrics = metricsService.getServiceMetrics(SERVICE_NAME);
            assertNotNull(serviceMetrics);
            assertTrue(serviceMetrics.getRequestCount() >= 0);
            assertTrue(serviceMetrics.getErrorCount() >= 0);
        });
    }

    @Test
    @DisplayName("Should track total requests and errors correctly")
    void shouldTrackTotalRequestsAndErrorsCorrectly() {
        // Given
        metricsService.recordRequest(SERVICE_NAME, 100L, 1024L);
        metricsService.recordRequest(SERVICE_NAME, 200L, 2048L);
        metricsService.recordError(SERVICE_NAME);

        // When
        long totalRequests = metricsService.getTotalRequests().sum();
        long totalErrors = metricsService.getTotalErrors().sum();

        // Then
        assertEquals(2, totalRequests);
        assertEquals(1, totalErrors);
    }
}
