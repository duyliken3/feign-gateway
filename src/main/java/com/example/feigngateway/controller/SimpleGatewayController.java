package com.example.feigngateway.controller;

import com.example.feigngateway.service.GatewayService;
import com.example.feigngateway.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
public class SimpleGatewayController {
    
    private final GatewayService gatewayService;
    private final StreamingService streamingService;
    
    @RequestMapping(value = "/{service}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Object> handleRequest(@PathVariable("service") String service,
            @RequestParam Map<String, String> queryParams, @RequestBody(required = false) Object body,
            HttpServletRequest request) {
        return gatewayService.forwardRequest(service, extractPathInService(request.getRequestURI(), service), 
            request.getMethod(), queryParams, body);
    }
    
    @GetMapping(value = "/{service}/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> stream(@PathVariable("service") String service,
            @RequestParam Map<String, String> queryParams, HttpServletRequest request) {
        return streamingService.streamResponse(service, extractPathInService(request.getRequestURI(), service), queryParams);
    }
    
    @RequestMapping(value = "/{service}/**", method = {RequestMethod.POST, RequestMethod.PUT}, 
                   consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadMultipart(@PathVariable("service") String service,
            @RequestParam Map<String, String> queryParams, @RequestParam(required = false) Map<String, String> form,
            @RequestPart(required = false) MultipartFile[] files, HttpServletRequest request) {
        return gatewayService.forwardMultipartRequest(service, extractPathInService(request.getRequestURI(), service), 
            request.getMethod(), queryParams, form, files);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Simple Gateway is running");
    }
    
    private String extractPathInService(String requestPath, String service) {
        return requestPath.replaceFirst("^/api/execution/" + service, "");
    }
}
