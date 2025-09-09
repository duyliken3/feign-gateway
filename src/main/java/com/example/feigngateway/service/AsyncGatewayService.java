package com.example.feigngateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncGatewayService {
    
    private final RestTemplate restTemplate;
    private final CacheService cacheService;
    
    @Async("gatewayTaskExecutor")
    public CompletableFuture<ResponseEntity<Object>> forwardRequestAsync(String service, String pathInService, 
                                                                        String method, Map<String, String> queryParams, 
                                                                        Object body) {
        try {
            log.debug("Processing async request for service: {}, path: {}", service, pathInService);
            
            // Use cached validation
            if (!cacheService.isRequestAllowedCached(service, pathInService)) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.status(403).body("Access denied: Path not whitelisted"));
            }
            
            String targetUrl = buildTargetUrl(service, pathInService);
            if (targetUrl == null) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("Invalid service path"));
            }
            
            String finalUrl = buildUrlWithQueryParams(targetUrl, queryParams);
            Object response = makeRequest(method, finalUrl, body);
            
            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
            
        } catch (Exception e) {
            log.error("Error processing async request", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(500).body("Internal server error: " + e.getMessage()));
        }
    }
    
    @Async("gatewayTaskExecutor")
    public CompletableFuture<ResponseEntity<Object>> forwardMultipartRequestAsync(String service, String pathInService,
                                                                                  String method, Map<String, String> queryParams,
                                                                                  Map<String, String> form, MultipartFile[] files) {
        try {
            log.debug("Processing async multipart request for service: {}, path: {}", service, pathInService);
            
            if (!cacheService.isRequestAllowedCached(service, pathInService)) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.status(403).body("Access denied: Path not whitelisted"));
            }
            
            String targetUrl = buildTargetUrl(service, pathInService);
            if (targetUrl == null) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("Invalid service path"));
            }
            
            String finalUrl = buildUrlWithQueryParams(targetUrl, queryParams);
            MultiValueMap<String, Object> multipartBody = buildMultipartBody(form, files);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(multipartBody, headers);
            
            HttpMethod httpMethod = "PUT".equalsIgnoreCase(method) ? HttpMethod.PUT : HttpMethod.POST;
            Object response = restTemplate.exchange(finalUrl, httpMethod, entity, Object.class).getBody();
            
            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
            
        } catch (Exception e) {
            log.error("Error processing async multipart request", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(500).body("Internal server error: " + e.getMessage()));
        }
    }
    
    private String buildTargetUrl(String service, String pathInService) {
        var serviceConfig = cacheService.getCachedServiceConfig(service);
        return serviceConfig != null ? serviceConfig.getBaseUrl() + pathInService : null;
    }
    
    private Object makeRequest(String method, String url, Object body) {
        switch (method.toUpperCase()) {
            case "GET":
                return restTemplate.getForObject(url, Object.class);
            case "POST":
                return restTemplate.postForObject(url, body, Object.class);
            case "PUT":
                return restTemplate.exchange(url, HttpMethod.PUT, 
                    new HttpEntity<>(body), Object.class).getBody();
            case "DELETE":
                return restTemplate.exchange(url, HttpMethod.DELETE, 
                    new HttpEntity<>(body), Object.class).getBody();
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }
    
    private String buildUrlWithQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return url;
        
        var builder = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(url);
        queryParams.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
            .forEach(entry -> builder.queryParam(entry.getKey(), entry.getValue()));
        return builder.toUriString();
    }
    
    private MultiValueMap<String, Object> buildMultipartBody(Map<String, String> form, MultipartFile[] files) {
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        
        if (form != null) form.forEach(multipartBody::add);
        
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        multipartBody.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() { return file.getOriginalFilename(); }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        
        return multipartBody;
    }
}
