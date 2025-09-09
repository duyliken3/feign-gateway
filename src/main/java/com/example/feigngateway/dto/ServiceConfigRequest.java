package com.example.feigngateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceConfigRequest {
    
    @NotBlank(message = "Service name is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Service name must contain only alphanumeric characters, hyphens, and underscores")
    @Size(min = 1, max = 50, message = "Service name must be between 1 and 50 characters")
    private String name;
    
    @NotBlank(message = "Base URL is required")
    @Pattern(regexp = "^https?://[a-zA-Z0-9.-]+(?:\\:[0-9]+)?(?:/.*)?$", message = "Base URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    private String baseUrl;
    
    @NotNull(message = "Endpoints list is required")
    @Size(min = 1, message = "At least one endpoint must be specified")
    private List<@Pattern(regexp = "^/[a-zA-Z0-9/_.{}-]*$", message = "Endpoint must be a valid path pattern") String> endpoints;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    private boolean enabled = true;
    
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Version must contain only alphanumeric characters, hyphens, and underscores")
    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String version;
}
