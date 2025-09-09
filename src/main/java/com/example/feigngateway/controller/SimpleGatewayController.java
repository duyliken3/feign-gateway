package com.example.feigngateway.controller;

import com.example.feigngateway.dto.ErrorResponse;
import com.example.feigngateway.service.GatewayService;
import com.example.feigngateway.service.StreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Gateway", description = "Universal API Gateway for microservices routing")
public class SimpleGatewayController {
    
    private final GatewayService gatewayService;
    private final StreamingService streamingService;
    
    @RequestMapping(value = "/{service}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @Operation(
        summary = "Universal Request Handler",
        description = """
            Forward requests to any whitelisted service.
            
            **Path Structure:** `/api/execution/{service}/{endpoint-path}`
            
            **Supported Services:**
            - `user-service` - User management
            - `post-service` - Post management
            - `comment-service` - Comment management
            
            **Examples:**
            - `GET /api/execution/user-service/users` - Get all users
            - `POST /api/execution/user-service/users` - Create user
            - `PUT /api/execution/user-service/users/1` - Update user
            - `DELETE /api/execution/user-service/users/1` - Delete user
            """,
        tags = {"Universal Routing"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful response from target service",
            content = @Content(schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "201", description = "Resource created successfully",
            content = @Content(schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Service not whitelisted or endpoint not allowed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Service or resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> handleRequest(
            @Parameter(description = "Target service name (must be whitelisted)", 
                      example = "user-service", 
                      required = true)
            @PathVariable("service") String service,
            
            @Parameter(description = "Query parameters to forward to target service")
            @RequestParam Map<String, String> queryParams, 
            
            @Parameter(description = "Request body to forward to target service")
            @RequestBody(required = false) Object body,
            
            HttpServletRequest request) {
        return gatewayService.forwardRequest(service, extractPathInService(request.getRequestURI(), service), 
            request.getMethod(), queryParams, body);
    }
    
    @GetMapping(value = "/{service}/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Stream Large Response",
        description = """
            Stream large responses from any whitelisted service.
            
            **Use Cases:**
            - Large file downloads
            - Streaming data responses
            - Real-time data feeds
            
            **Headers Required:**
            - `Accept: application/octet-stream`
            """,
        tags = {"Streaming"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Streaming response",
            content = @Content(mediaType = "application/octet-stream", 
                             schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Service not whitelisted or endpoint not allowed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StreamingResponseBody> stream(
            @Parameter(description = "Target service name (must be whitelisted)", 
                      example = "user-service", 
                      required = true)
            @PathVariable("service") String service,
            
            @Parameter(description = "Query parameters to forward to target service")
            @RequestParam Map<String, String> queryParams, 
            
            HttpServletRequest request) {
        return streamingService.streamResponse(service, extractPathInService(request.getRequestURI(), service), queryParams);
    }
    
    @RequestMapping(value = "/{service}/**", method = {RequestMethod.POST, RequestMethod.PUT}, 
                   consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload Files (Multipart)",
        description = """
            Upload files to any whitelisted service using multipart form data.
            
            **Use Cases:**
            - Profile picture uploads
            - Document uploads
            - Media file uploads
            
            **Content-Type Required:**
            - `multipart/form-data`
            """,
        tags = {"File Upload"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files uploaded successfully",
            content = @Content(schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Service not whitelisted or endpoint not allowed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "413", description = "File too large",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> uploadMultipart(
            @Parameter(description = "Target service name (must be whitelisted)", 
                      example = "user-service", 
                      required = true)
            @PathVariable("service") String service,
            
            @Parameter(description = "Query parameters to forward to target service")
            @RequestParam Map<String, String> queryParams, 
            
            @Parameter(description = "Form data fields")
            @RequestParam(required = false) Map<String, String> form,
            
            @Parameter(description = "Files to upload")
            @RequestPart(required = false) MultipartFile[] files, 
            
            HttpServletRequest request) {
        return gatewayService.forwardMultipartRequest(service, extractPathInService(request.getRequestURI(), service), 
            request.getMethod(), queryParams, form, files);
    }
    
    @GetMapping("/health")
    @Operation(
        summary = "Gateway Health Check",
        description = "Check if the gateway is running and healthy",
        tags = {"Health"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gateway is healthy and running",
            content = @Content(mediaType = "text/plain", 
                             schema = @Schema(type = "string"),
                             examples = @ExampleObject(value = "Simple Gateway is running"))),
        @ApiResponse(responseCode = "500", description = "Gateway is unhealthy",
            content = @Content(mediaType = "text/plain", 
                             schema = @Schema(type = "string")))
    })
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Simple Gateway is running");
    }
    
    private String extractPathInService(String requestPath, String service) {
        return requestPath.replaceFirst("^/api/execution/" + service, "");
    }
}
