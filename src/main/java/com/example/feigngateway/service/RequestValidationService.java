package com.example.feigngateway.service;

import com.example.feigngateway.dto.GatewayRequest;
import com.example.feigngateway.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestValidationService {
    
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_.-]*$");
    private static final Pattern HTTP_METHOD_PATTERN = Pattern.compile("^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[a-zA-Z0-9.-]+(?:\\:[0-9]+)?(?:/.*)?$");
    
    private static final int MAX_SERVICE_NAME_LENGTH = 50;
    private static final int MAX_PATH_LENGTH = 500;
    private static final int MAX_QUERY_PARAMS = 100;
    private static final int MAX_HEADER_SIZE = 8192;
    
    public void validateGatewayRequest(GatewayRequest request) {
        List<String> errors = new ArrayList<>();
        
        validateServiceName(request.getService(), errors);
        validatePath(request.getPath(), errors);
        validateMethod(request.getMethod(), errors);
        validateQueryParams(request.getQueryParams(), errors);
        validateHeaders(request.getHeaders(), errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Request validation failed", errors);
        }
    }
    
    public void validateServiceName(String serviceName) {
        List<String> errors = new ArrayList<>();
        validateServiceName(serviceName, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid service name", errors);
        }
    }
    
    public void validatePath(String path) {
        List<String> errors = new ArrayList<>();
        validatePath(path, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid path", errors);
        }
    }
    
    public void validateHttpMethod(String method) {
        List<String> errors = new ArrayList<>();
        validateMethod(method, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid HTTP method", errors);
        }
    }
    
    private void validateServiceName(String serviceName, List<String> errors) {
        if (!StringUtils.hasText(serviceName)) {
            errors.add("Service name is required");
            return;
        }
        
        if (serviceName.length() > MAX_SERVICE_NAME_LENGTH) {
            errors.add("Service name must not exceed " + MAX_SERVICE_NAME_LENGTH + " characters");
        }
        
        if (!SERVICE_NAME_PATTERN.matcher(serviceName).matches()) {
            errors.add("Service name must contain only alphanumeric characters, hyphens, and underscores");
        }
    }
    
    private void validatePath(String path, List<String> errors) {
        if (!StringUtils.hasText(path)) {
            errors.add("Path is required");
            return;
        }
        
        if (path.length() > MAX_PATH_LENGTH) {
            errors.add("Path must not exceed " + MAX_PATH_LENGTH + " characters");
        }
        
        if (!PATH_PATTERN.matcher(path).matches()) {
            errors.add("Path must start with / and contain only valid path characters");
        }
    }
    
    private void validateMethod(String method, List<String> errors) {
        if (!StringUtils.hasText(method)) {
            errors.add("HTTP method is required");
            return;
        }
        
        if (!HTTP_METHOD_PATTERN.matcher(method.toUpperCase()).matches()) {
            errors.add("HTTP method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS");
        }
    }
    
    private void validateQueryParams(java.util.Map<String, String> queryParams, List<String> errors) {
        if (queryParams == null) {
            return;
        }
        
        if (queryParams.size() > MAX_QUERY_PARAMS) {
            errors.add("Too many query parameters. Maximum allowed: " + MAX_QUERY_PARAMS);
        }
        
        queryParams.forEach((key, value) -> {
            if (!StringUtils.hasText(key)) {
                errors.add("Query parameter key cannot be empty");
            }
            if (key.length() > 100) {
                errors.add("Query parameter key '" + key + "' exceeds maximum length of 100 characters");
            }
            if (value != null && value.length() > 1000) {
                errors.add("Query parameter value for '" + key + "' exceeds maximum length of 1000 characters");
            }
        });
    }
    
    private void validateHeaders(java.util.Map<String, String> headers, List<String> errors) {
        if (headers == null) {
            return;
        }
        
        headers.forEach((key, value) -> {
            if (!StringUtils.hasText(key)) {
                errors.add("Header name cannot be empty");
            }
            if (key.length() > 100) {
                errors.add("Header name '" + key + "' exceeds maximum length of 100 characters");
            }
            if (value != null && value.length() > MAX_HEADER_SIZE) {
                errors.add("Header value for '" + key + "' exceeds maximum size of " + MAX_HEADER_SIZE + " bytes");
            }
        });
    }
    
    public boolean isValidServiceName(String serviceName) {
        return StringUtils.hasText(serviceName) && 
               serviceName.length() <= MAX_SERVICE_NAME_LENGTH && 
               SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }
    
    public boolean isValidPath(String path) {
        return StringUtils.hasText(path) && 
               path.length() <= MAX_PATH_LENGTH && 
               PATH_PATTERN.matcher(path).matches();
    }
    
    public boolean isValidHttpMethod(String method) {
        return StringUtils.hasText(method) && 
               HTTP_METHOD_PATTERN.matcher(method.toUpperCase()).matches();
    }
    
    public boolean isValidUrl(String url) {
        return StringUtils.hasText(url) && URL_PATTERN.matcher(url).matches();
    }
}
