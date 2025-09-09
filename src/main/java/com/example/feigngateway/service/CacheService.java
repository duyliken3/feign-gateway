package com.example.feigngateway.service;

import com.example.feigngateway.config.GatewayWhitelistProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final GatewayWhitelistProperties whitelistProperties;
    
    // In-memory cache for compiled regex patterns
    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();
    
    // In-memory cache for service configurations
    private final ConcurrentHashMap<String, GatewayWhitelistProperties.ServiceConfig> serviceConfigCache = new ConcurrentHashMap<>();
    
    @Cacheable(value = "serviceConfigs", key = "#serviceName")
    public GatewayWhitelistProperties.ServiceConfig getCachedServiceConfig(String serviceName) {
        log.debug("Loading service config for: {}", serviceName);
        
        List<GatewayWhitelistProperties.ServiceConfig> services = whitelistProperties.getServices();
        if (services == null) {
            return null;
        }
        
        return services.stream()
                .filter(service -> serviceName != null && serviceName.equals(service.getName()))
                .findFirst()
                .orElse(null);
    }
    
    @Cacheable(value = "pathPatterns", key = "#endpoint")
    public Pattern getCompiledPattern(String endpoint) {
        log.debug("Compiling pattern for endpoint: {}", endpoint);
        
        // Convert Spring path pattern to regex
        String regex = endpoint
                .replace("**", ".*")  // ** matches any number of path segments including /
                .replace("*", "[^/]*")  // * matches any characters except /
                .replace("{", "(")
                .replace("}", ")");
        
        // Ensure the regex matches the entire path
        if (!regex.startsWith("^")) {
            regex = "^" + regex;
        }
        if (!regex.endsWith("$")) {
            regex = regex + "$";
        }
        
        return Pattern.compile(regex);
    }
    
    @Cacheable(value = "whitelistValidation", key = "#serviceName + ':' + #pathInService")
    public boolean isRequestAllowedCached(String serviceName, String pathInService) {
        log.debug("Checking whitelist for service: {}, path: {}", serviceName, pathInService);
        
        if (!whitelistProperties.isEnabled()) {
            return true;
        }

        GatewayWhitelistProperties.ServiceConfig service = getCachedServiceConfig(serviceName);
        if (service == null) {
            return false;
        }

        return isPathMatchedCached(pathInService, service.getEndpoints());
    }
    
    private boolean isPathMatchedCached(String requestPath, List<String> allowedEndpoints) {
        if (allowedEndpoints == null || allowedEndpoints.isEmpty()) {
            return false;
        }
        
        for (String endpoint : allowedEndpoints) {
            Pattern pattern = getCompiledPattern(endpoint);
            if (pattern.matcher(requestPath).matches()) {
                return true;
            }
        }
        
        return false;
    }
    
    // Cache warming methods
    public void warmUpCaches() {
        log.info("Warming up caches...");
        
        List<GatewayWhitelistProperties.ServiceConfig> services = whitelistProperties.getServices();
        if (services != null) {
            services.forEach(service -> {
                getCachedServiceConfig(service.getName());
                if (service.getEndpoints() != null) {
                    service.getEndpoints().forEach(this::getCompiledPattern);
                }
            });
        }
        
        log.info("Cache warming completed");
    }
    
    // Cache statistics
    public CacheStats getCacheStats() {
        return CacheStats.builder()
                .patternCacheSize(patternCache.size())
                .serviceConfigCacheSize(serviceConfigCache.size())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private int patternCacheSize;
        private int serviceConfigCacheSize;
    }
}
