package com.example.feigngateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GatewayService {
    
    private final RestTemplate restTemplate;
    private final WhitelistService whitelistService;
    
    public ResponseEntity<Object> forwardRequest(String service, String pathInService, 
                                               String method, Map<String, String> queryParams, 
                                               Object body) {
        return validateAndExecute(service, pathInService, () -> {
            String targetUrl = whitelistService.getTargetUrl(service, pathInService);
            String finalUrl = buildUrlWithQueryParams(targetUrl, queryParams);
            return ResponseEntity.ok(makeRequest(method, finalUrl, body));
        });
    }
    
    public ResponseEntity<Object> forwardMultipartRequest(String service, String pathInService,
                                                         String method, Map<String, String> queryParams,
                                                         Map<String, String> form, MultipartFile[] files) {
        return validateAndExecute(service, pathInService, () -> {
            String targetUrl = whitelistService.getTargetUrl(service, pathInService);
            String finalUrl = buildUrlWithQueryParams(targetUrl, queryParams);
            MultiValueMap<String, Object> multipartBody = buildMultipartBody(form, files);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(multipartBody, headers);
            
            HttpMethod httpMethod = "PUT".equalsIgnoreCase(method) ? HttpMethod.PUT : HttpMethod.POST;
            Object response = restTemplate.exchange(finalUrl, httpMethod, entity, Object.class).getBody();
            
            return ResponseEntity.ok(response);
        });
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
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
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
    
    private ResponseEntity<Object> validateAndExecute(String service, String pathInService, 
                                                    java.util.function.Supplier<ResponseEntity<Object>> executor) {
        if (!whitelistService.isRequestAllowed(service, pathInService)) {
            return ResponseEntity.status(403).body("Access denied: Path not whitelisted");
        }
        
        String targetUrl = whitelistService.getTargetUrl(service, pathInService);
        if (targetUrl == null) {
            return ResponseEntity.badRequest().body("Invalid service path");
        }
        
        return executor.get();
    }
}
