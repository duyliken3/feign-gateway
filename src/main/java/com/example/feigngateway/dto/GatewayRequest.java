package com.example.feigngateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayRequest {
    
    @NotBlank(message = "Service name is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Service name must contain only alphanumeric characters, hyphens, and underscores")
    @Size(min = 1, max = 50, message = "Service name must be between 1 and 50 characters")
    private String service;
    
    @Pattern(regexp = "^/[a-zA-Z0-9/_.-]*$", message = "Path must start with / and contain only valid path characters")
    @Size(max = 500, message = "Path must not exceed 500 characters")
    private String path;
    
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$", message = "HTTP method must be valid")
    private String method;
    
    private Map<String, String> queryParams;
    
    private Object body;
    
    private Map<String, String> headers;
    
    @Size(max = 100, message = "Content type must not exceed 100 characters")
    private String contentType;
    
    @Size(max = 1000, message = "User agent must not exceed 1000 characters")
    private String userAgent;
}
