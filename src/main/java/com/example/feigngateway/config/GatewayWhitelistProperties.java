package com.example.feigngateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.whitelist")
public class GatewayWhitelistProperties {
    
    private boolean enabled = true;
    private List<ServiceConfig> services;
    
    @Data
    public static class ServiceConfig {
        private String name;
        private String baseUrl;
        private List<String> endpoints;
    }
}
