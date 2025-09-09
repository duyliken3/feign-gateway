package com.example.feigngateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Service
@Slf4j
public class PerformanceMetricsService {
    
    private final ConcurrentHashMap<String, ServiceMetrics> serviceMetrics = new ConcurrentHashMap<>();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalErrors = new LongAdder();
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    
    // Getters for external access
    public LongAdder getTotalRequests() { return totalRequests; }
    public LongAdder getTotalErrors() { return totalErrors; }
    public AtomicLong getStartTime() { return startTime; }
    
    public static class ServiceMetrics {
        private final LongAdder requestCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final LongAdder totalResponseTime = new LongAdder();
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final LongAdder totalBytesTransferred = new LongAdder();
        
        public void recordRequest(long responseTimeMs, long bytesTransferred) {
            requestCount.increment();
            totalResponseTime.add(responseTimeMs);
            totalBytesTransferred.add(bytesTransferred);
            
            // Update min/max response times
            long currentMin = minResponseTime.get();
            while (responseTimeMs < currentMin && !minResponseTime.compareAndSet(currentMin, responseTimeMs)) {
                currentMin = minResponseTime.get();
            }
            
            long currentMax = maxResponseTime.get();
            while (responseTimeMs > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTimeMs)) {
                currentMax = maxResponseTime.get();
            }
        }
        
        public void recordError() {
            errorCount.increment();
        }
        
        public long getRequestCount() {
            return requestCount.sum();
        }
        
        public long getErrorCount() {
            return errorCount.sum();
        }
        
        public double getAverageResponseTime() {
            long requests = requestCount.sum();
            return requests > 0 ? (double) totalResponseTime.sum() / requests : 0.0;
        }
        
        public long getMinResponseTime() {
            long min = minResponseTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxResponseTime() {
            return maxResponseTime.get();
        }
        
        public long getTotalBytesTransferred() {
            return totalBytesTransferred.sum();
        }
        
        public double getErrorRate() {
            long requests = requestCount.sum();
            return requests > 0 ? (double) errorCount.sum() / requests * 100 : 0.0;
        }
    }
    
    public void recordRequest(String serviceName, long responseTimeMs, long bytesTransferred) {
        totalRequests.increment();
        serviceMetrics.computeIfAbsent(serviceName, k -> new ServiceMetrics())
                .recordRequest(responseTimeMs, bytesTransferred);
    }
    
    public void recordError(String serviceName) {
        totalErrors.increment();
        serviceMetrics.computeIfAbsent(serviceName, k -> new ServiceMetrics())
                .recordError();
    }
    
    public ServiceMetrics getServiceMetrics(String serviceName) {
        return serviceMetrics.getOrDefault(serviceName, new ServiceMetrics());
    }
    
    public String getOverallStats() {
        long uptime = System.currentTimeMillis() - startTime.get();
        long totalReqs = totalRequests.sum();
        long totalErrs = totalErrors.sum();
        
        return String.format("""
            Gateway Performance Statistics:
            Uptime: %d ms (%.2f hours)
            Total Requests: %d
            Total Errors: %d
            Error Rate: %.2f%%
            Requests per Second: %.2f
            """,
            uptime, uptime / 3600000.0,
            totalReqs, totalErrs,
            totalReqs > 0 ? (double) totalErrs / totalReqs * 100 : 0.0,
            uptime > 0 ? (double) totalReqs / (uptime / 1000.0) : 0.0
        );
    }
    
    public String getServiceStats(String serviceName) {
        ServiceMetrics metrics = getServiceMetrics(serviceName);
        
        return String.format("""
            Service: %s
            Requests: %d
            Errors: %d
            Error Rate: %.2f%%
            Avg Response Time: %.2f ms
            Min Response Time: %d ms
            Max Response Time: %d ms
            Total Bytes: %d
            """,
            serviceName,
            metrics.getRequestCount(),
            metrics.getErrorCount(),
            metrics.getErrorRate(),
            metrics.getAverageResponseTime(),
            metrics.getMinResponseTime(),
            metrics.getMaxResponseTime(),
            metrics.getTotalBytesTransferred()
        );
    }
    
    public void resetMetrics() {
        serviceMetrics.clear();
        totalRequests.reset();
        totalErrors.reset();
        startTime.set(System.currentTimeMillis());
        log.info("Performance metrics reset");
    }
}
