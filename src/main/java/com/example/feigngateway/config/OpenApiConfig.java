package com.example.feigngateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Feign Gateway API")
                        .description("""
                                Universal Spring Boot API Gateway with Feign Client and Whitelist Security.
                                
                                This gateway provides a centralized entry point for microservices communication with:
                                - Dynamic service routing based on path patterns
                                - Whitelist-based security validation
                                - Request/response logging and monitoring
                                - Streaming support for large responses
                                - Multipart file upload handling
                                
                                ## How it works
                                
                                All requests follow the pattern: `/api/execution/{service-name}/{endpoint-path}`
                                
                                The gateway automatically:
                                1. Extracts the service name from the URL path
                                2. Validates the service against the whitelist
                                3. Routes the request to the appropriate backend service
                                4. Returns the response to the client
                                
                                ## Supported Services
                                
                                - **user-service**: User management operations
                                - **post-service**: Post and content management
                                - **comment-service**: Comment and discussion features
                                """)
                        .version("1.4.0")
                        .contact(new Contact()
                                .name("Ngoc Duy Tran")
                                .url("https://github.com/duyliken3")
                                .email("duyliken3@github.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production server")
                ));
    }
}
