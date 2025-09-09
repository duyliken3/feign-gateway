package com.example.feigngateway.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    
    @NotNull
    private Whitelist whitelist = new Whitelist();
    
    @NotNull
    private Performance performance = new Performance();
    
    @NotNull
    private Security security = new Security();
    
    @NotNull
    private Monitoring monitoring = new Monitoring();
    
    @Data
    public static class Whitelist {
        private boolean enabled = true;
        private List<ServiceConfig> services = List.of();
    }
    
    @Data
    public static class ServiceConfig {
        @NotBlank
        private String name;
        
        @NotBlank
        private String baseUrl;
        
        @NotNull
        private List<String> endpoints = List.of();
        
        private String description;
        private boolean enabled = true;
        private String version;
    }
    
    @Data
    public static class Performance {
        @NotNull
        private ConnectionPool connectionPool = new ConnectionPool();
        
        @NotNull
        private ThreadPool threadPool = new ThreadPool();
        
        @NotNull
        private Cache cache = new Cache();
        
        @NotNull
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
        
        @NotNull
        private RateLimiting rateLimiting = new RateLimiting();
        
        @Data
        public static class ConnectionPool {
            @Min(1)
            @Max(1000)
            private int maxTotal = 500;
            
            @Min(1)
            @Max(100)
            private int maxPerRoute = 100;
            
            @Min(1000)
            @Max(30000)
            private int validateAfterInactivity = 2000;
            
            @Min(10)
            @Max(300)
            private int keepAliveTime = 30;
        }
        
        @Data
        public static class ThreadPool {
            @Min(1)
            @Max(50)
            private int coreSize = 20;
            
            @Min(10)
            @Max(200)
            private int maxSize = 100;
            
            @Min(100)
            @Max(1000)
            private int queueCapacity = 500;
            
            @Min(30)
            @Max(300)
            private int keepAliveSeconds = 60;
        }
        
        @Data
        public static class Cache {
            private boolean enabled = true;
            
            @Min(60)
            @Max(3600)
            private int ttl = 300;
            
            @Min(100)
            @Max(10000)
            private int maxSize = 1000;
        }
        
        @Data
        public static class CircuitBreaker {
            private boolean enabled = true;
            
            @Min(1)
            @Max(20)
            private int failureThreshold = 5;
            
            @Min(10000)
            @Max(300000)
            private long timeoutDuration = 60000;
            
            @Min(1)
            @Max(10)
            private int successThreshold = 3;
        }
        
        @Data
        public static class RateLimiting {
            private boolean enabled = false;
            
            @Min(1)
            @Max(10000)
            private int requestsPerMinute = 1000;
            
            @Min(10)
            @Max(1000)
            private int burstCapacity = 100;
        }
    }
    
    @Data
    public static class Security {
        private boolean enabled = true;
        private boolean corsEnabled = false;
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH");
        private List<String> allowedHeaders = List.of("*");
        private boolean credentialsAllowed = false;
        private Duration maxAge = Duration.ofHours(1);
    }
    
    @Data
    public static class Monitoring {
        private boolean enabled = true;
        
        @Min(300)
        @Max(86400)
        private int metricsRetention = 3600;
        
        @Min(10)
        @Max(300)
        private int healthCheckInterval = 30;
        
        private boolean detailedLogging = false;
        private boolean performanceMetrics = true;
    }
}
