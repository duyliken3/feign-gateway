package com.example.feigngateway.controller;

import com.example.feigngateway.service.CacheService;
import com.example.feigngateway.service.CircuitBreakerService;
import com.example.feigngateway.service.PerformanceMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "Performance", description = "Performance monitoring and metrics endpoints")
public class PerformanceController {
    
    private final PerformanceMetricsService metricsService;
    private final CircuitBreakerService circuitBreakerService;
    private final CacheService cacheService;
    
    @GetMapping("/stats")
    @Operation(summary = "Get overall performance statistics", 
               description = "Returns comprehensive performance metrics for the gateway")
    public ResponseEntity<Map<String, Object>> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("overall", metricsService.getOverallStats());
        stats.put("circuitBreakers", circuitBreakerService.getCircuitBreakerStats());
        stats.put("cacheStats", cacheService.getCacheStats());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/service/{serviceName}")
    @Operation(summary = "Get service-specific performance statistics",
               description = "Returns performance metrics for a specific service")
    public ResponseEntity<Map<String, Object>> getServiceStats(@PathVariable String serviceName) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("service", serviceName);
        stats.put("metrics", metricsService.getServiceStats(serviceName));
        stats.put("circuitState", circuitBreakerService.getCircuitState(serviceName));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/circuit-breakers")
    @Operation(summary = "Get circuit breaker status",
               description = "Returns the status of all circuit breakers")
    public ResponseEntity<String> getCircuitBreakerStatus() {
        return ResponseEntity.ok(circuitBreakerService.getCircuitBreakerStats());
    }
    
    @GetMapping("/cache")
    @Operation(summary = "Get cache statistics",
               description = "Returns cache performance and usage statistics")
    public ResponseEntity<CacheService.CacheStats> getCacheStats() {
        return ResponseEntity.ok(cacheService.getCacheStats());
    }
    
    @GetMapping("/health")
    @Operation(summary = "Get performance health status",
               description = "Returns the health status of performance monitoring systems")
    public ResponseEntity<Map<String, Object>> getPerformanceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", System.currentTimeMillis() - metricsService.getStartTime().get());
        health.put("totalRequests", metricsService.getTotalRequests().sum());
        health.put("totalErrors", metricsService.getTotalErrors().sum());
        
        return ResponseEntity.ok(health);
    }
}
