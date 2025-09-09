package com.example.feigngateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response structure")
public class ErrorResponse {
    
    @Schema(description = "Indicates if the request was successful", example = "false")
    private boolean success;
    
    @Schema(description = "Error message describing what went wrong", 
            example = "Access denied: Path not whitelisted")
    private String message;
    
    @Schema(description = "Additional error data (usually null for errors)", 
            example = "null", 
            nullable = true)
    private Object data;
    
    @Schema(description = "HTTP status code", example = "403")
    private int statusCode;
    
    @Schema(description = "Timestamp when the error occurred", 
            example = "2025-01-27T10:30:00Z")
    private Instant timestamp;
    
    public static ErrorResponse of(String message, int statusCode) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }
    
    public static ErrorResponse of(String message, int statusCode, Object data) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }
}
