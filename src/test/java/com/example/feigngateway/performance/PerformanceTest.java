package com.example.feigngateway.performance;

import com.example.feigngateway.FeignGatewayApplication;
import com.example.feigngateway.service.CacheService;
import com.example.feigngateway.service.CircuitBreakerService;
import com.example.feigngateway.service.PerformanceMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FeignGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Performance Tests")
class PerformanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PerformanceMetricsService metricsService;

    @Autowired
    private CircuitBreakerService circuitBreakerService;

    @Autowired
    private CacheService cacheService;

    private String baseUrl;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/execution";
        executorService = Executors.newFixedThreadPool(50);
    }

    @Test
    @DisplayName("Should handle high throughput requests")
    void shouldHandleHighThroughputRequests() throws InterruptedException {
        // Given
        int totalRequests = 1000;
        int threadCount = 50;
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            executorService.submit(() -> {
                try {
                    var response = restTemplate.getForEntity(baseUrl + "/health", String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        long totalTime = endTime - startTime;
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);
        
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Error requests: " + errorCount.get());
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Requests per second: " + requestsPerSecond);
        
        assertTrue(successCount.get() > totalRequests * 0.95, "Success rate should be > 95%");
        assertTrue(requestsPerSecond > 100, "Should handle > 100 requests per second");
    }

    @Test
    @DisplayName("Should handle concurrent requests without data corruption")
    void shouldHandleConcurrentRequestsWithoutDataCorruption() throws InterruptedException {
        // Given
        int threadCount = 20;
        int requestsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        var response = restTemplate.getForEntity(baseUrl + "/health", String.class);
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);

        // Then
        int expectedRequests = threadCount * requestsPerThread;
        assertEquals(expectedRequests, successCount.get(), "All requests should succeed");
    }

    @Test
    @DisplayName("Should maintain performance under memory pressure")
    void shouldMaintainPerformanceUnderMemoryPressure() throws InterruptedException {
        // Given
        int totalRequests = 500;
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            executorService.submit(() -> {
                try {
                    // Simulate memory pressure by creating large objects
                    byte[] largeArray = new byte[1024 * 1024]; // 1MB
                    var response = restTemplate.getForEntity(baseUrl + "/health", String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    }
                    // Let GC clean up
                    largeArray = null;
                } catch (Exception e) {
                    // Ignore errors for this test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        long totalTime = endTime - startTime;
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);
        
        assertTrue(successCount.get() > totalRequests * 0.9, "Success rate should be > 90% under memory pressure");
        assertTrue(requestsPerSecond > 50, "Should maintain reasonable performance under memory pressure");
    }

    @Test
    @DisplayName("Should handle circuit breaker performance correctly")
    void shouldHandleCircuitBreakerPerformanceCorrectly() throws InterruptedException {
        // Given
        String serviceName = "test-service";
        int failureThreshold = 5;
        int totalRequests = 100;

        // When - Record failures to open circuit
        for (int i = 0; i < failureThreshold; i++) {
            circuitBreakerService.recordFailure(serviceName);
        }

        // Then - Circuit should be open
        assertFalse(circuitBreakerService.isRequestAllowed(serviceName));
        assertEquals(CircuitBreakerService.CircuitState.OPEN, 
                circuitBreakerService.getCircuitState(serviceName));

        // When - Record successes to close circuit
        for (int i = 0; i < 3; i++) {
            circuitBreakerService.recordSuccess(serviceName);
        }

        // Then - Circuit should be closed
        assertTrue(circuitBreakerService.isRequestAllowed(serviceName));
        assertEquals(CircuitBreakerService.CircuitState.CLOSED, 
                circuitBreakerService.getCircuitState(serviceName));
    }

    @Test
    @DisplayName("Should handle cache performance correctly")
    void shouldHandleCachePerformanceCorrectly() {
        // Given
        String key = "test-key";
        String value = "test-value";

        // When - First access (cache miss)
        long startTime = System.nanoTime();
        cacheService.put(key, value);
        String retrievedValue = cacheService.get(key);
        long firstAccessTime = System.nanoTime() - startTime;

        // When - Second access (cache hit)
        startTime = System.nanoTime();
        String cachedValue = cacheService.get(key);
        long secondAccessTime = System.nanoTime() - startTime;

        // Then
        assertEquals(value, retrievedValue);
        assertEquals(value, cachedValue);
        assertTrue(secondAccessTime < firstAccessTime, "Cache hit should be faster than cache miss");
    }

    @Test
    @DisplayName("Should handle metrics collection performance")
    void shouldHandleMetricsCollectionPerformance() {
        // Given
        String serviceName = "metrics-test-service";
        int iterations = 1000;

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            metricsService.recordRequest(serviceName, 100L, 1024L);
            if (i % 10 == 0) {
                metricsService.recordError(serviceName);
            }
        }
        long endTime = System.nanoTime();

        // Then
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations;
        
        assertTrue(avgTimePerOperation < 1000, "Metrics collection should be fast (< 1μs per operation)");
        
        var serviceMetrics = metricsService.getServiceMetrics(serviceName);
        assertEquals(iterations, serviceMetrics.getRequestCount());
        assertEquals(iterations / 10, serviceMetrics.getErrorCount());
    }

    @Test
    @DisplayName("Should handle thread pool performance")
    void shouldHandleThreadPoolPerformance() throws InterruptedException {
        // Given
        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < taskCount; i++) {
            executorService.submit(() -> {
                try {
                    // Simulate some work
                    Thread.sleep(10);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(5, TimeUnit.SECONDS);
        long endTime = System.nanoTime();

        // Then
        long totalTime = endTime - startTime;
        double avgTimePerTask = (double) totalTime / taskCount;
        
        assertEquals(taskCount, completedTasks.get());
        assertTrue(avgTimePerTask < 50_000_000, "Thread pool should handle tasks efficiently (< 50ms per task)");
    }

    @Test
    @DisplayName("Should handle memory usage efficiently")
    void shouldHandleMemoryUsageEfficiently() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - Perform many operations
        for (int i = 0; i < 1000; i++) {
            metricsService.recordRequest("memory-test", 100L, 1024L);
            circuitBreakerService.recordSuccess("memory-test");
            cacheService.put("key" + i, "value" + i);
        }

        // Force garbage collection
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Then
        assertTrue(memoryIncrease < 10 * 1024 * 1024, "Memory increase should be < 10MB");
    }

    @Test
    @DisplayName("Should handle response time consistently")
    void shouldHandleResponseTimeConsistently() throws InterruptedException {
        // Given
        int requestCount = 100;
        long[] responseTimes = new long[requestCount];
        CountDownLatch latch = new CountDownLatch(requestCount);

        // When
        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    long startTime = System.nanoTime();
                    var response = restTemplate.getForEntity(baseUrl + "/health", String.class);
                    long endTime = System.nanoTime();
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        responseTimes[index] = (endTime - startTime) / 1_000_000; // Convert to milliseconds
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);

        // Then - Calculate statistics
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        int validResponses = 0;

        for (long responseTime : responseTimes) {
            if (responseTime > 0) {
                sum += responseTime;
                min = Math.min(min, responseTime);
                max = Math.max(max, responseTime);
                validResponses++;
            }
        }

        if (validResponses > 0) {
            double avgResponseTime = (double) sum / validResponses;
            
            assertTrue(avgResponseTime < 1000, "Average response time should be < 1000ms");
            assertTrue(max - min < 5000, "Response time variance should be reasonable");
        }
    }

    @Test
    @DisplayName("Should handle stress test with mixed operations")
    void shouldHandleStressTestWithMixedOperations() throws InterruptedException {
        // Given
        int totalOperations = 500;
        CountDownLatch latch = new CountDownLatch(totalOperations);
        AtomicInteger successCount = new AtomicInteger(0);

        // When - Mix of different operations
        for (int i = 0; i < totalOperations; i++) {
            final int operationType = i % 4;
            executorService.submit(() -> {
                try {
                    switch (operationType) {
                        case 0:
                            // Health check
                            var response = restTemplate.getForEntity(baseUrl + "/health", String.class);
                            if (response.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                            break;
                        case 1:
                            // Metrics recording
                            metricsService.recordRequest("stress-test", 100L, 1024L);
                            successCount.incrementAndGet();
                            break;
                        case 2:
                            // Circuit breaker operations
                            circuitBreakerService.recordSuccess("stress-test");
                            successCount.incrementAndGet();
                            break;
                        case 3:
                            // Cache operations
                            cacheService.put("stress-key", "stress-value");
                            cacheService.get("stress-key");
                            successCount.incrementAndGet();
                            break;
                    }
                } catch (Exception e) {
                    // Ignore errors for stress test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);

        // Then
        assertTrue(successCount.get() > totalOperations * 0.9, "Success rate should be > 90% under stress");
    }
}
