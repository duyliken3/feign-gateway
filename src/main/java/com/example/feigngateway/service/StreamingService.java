package com.example.feigngateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StreamingService {
    
    private final RestTemplate restTemplate;
    private final WhitelistService whitelistService;
    
    public ResponseEntity<StreamingResponseBody> streamResponse(String service, String pathInService, 
                                                              Map<String, String> queryParams) {
        if (!whitelistService.isRequestAllowed(service, pathInService)) {
            return ResponseEntity.status(403).build();
        }
        
        String targetUrl = whitelistService.getTargetUrl(service, pathInService);
        if (targetUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String finalUrl = buildUrlWithQueryParams(targetUrl, queryParams);
        StreamingResponseBody body = outputStream -> 
            restTemplate.execute(finalUrl, HttpMethod.GET, null, 
                clientHttpResponse -> {
                    copyStream(clientHttpResponse.getBody(), outputStream);
                    return null;
                });
        
        return ResponseEntity.ok().body(body);
    }
    
    private String buildUrlWithQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return url;
        
        org.springframework.web.util.UriComponentsBuilder builder = 
            org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(url);
        queryParams.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
            .forEach(entry -> builder.queryParam(entry.getKey(), entry.getValue()));
        return builder.toUriString();
    }
    
    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (InputStream is = inputStream) {
            is.transferTo(outputStream);
        }
    }
}
