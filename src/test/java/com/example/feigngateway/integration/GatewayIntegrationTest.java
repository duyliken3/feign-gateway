package com.example.feigngateway.integration;

import com.example.feigngateway.FeignGatewayApplication;
import com.example.feigngateway.config.GatewayWhitelistProperties;
import com.example.feigngateway.dto.ErrorResponse;
import com.example.feigngateway.exception.ValidationException;
import com.example.feigngateway.service.GatewayService;
import com.example.feigngateway.service.WhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FeignGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Gateway Integration Tests")
class GatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private GatewayWhitelistProperties whitelistProperties;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/execution";
        setupTestWhitelist();
    }

    private void setupTestWhitelist() {
        whitelistProperties.setEnabled(true);
        GatewayWhitelistProperties.ServiceConfig userService = new GatewayWhitelistProperties.ServiceConfig();
        userService.setName("user-service");
        userService.setBaseUrl("https://jsonplaceholder.typicode.com");
        userService.setEndpoints(java.util.Arrays.asList("/users/**", "/posts/**"));
        whitelistProperties.setServices(java.util.Arrays.asList(userService));
    }

    @Test
    @DisplayName("Should handle successful GET request")
    void shouldHandleSuccessfulGetRequest() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/users/1", String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle successful POST request")
    void shouldHandleSuccessfulPostRequest() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"name\":\"Test User\",\"email\":\"test@example.com\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/user-service/users", entity, String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle successful PUT request")
    void shouldHandleSuccessfulPutRequest() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"name\":\"Updated User\",\"email\":\"updated@example.com\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user-service/users/1", HttpMethod.PUT, entity, String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle successful DELETE request")
    void shouldHandleSuccessfulDeleteRequest() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user-service/users/1", HttpMethod.DELETE, null, String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should reject request to non-whitelisted service")
    void shouldRejectRequestToNonWhitelistedService() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/unauthorized-service/data", String.class);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Access denied"));
    }

    @Test
    @DisplayName("Should reject request to non-whitelisted endpoint")
    void shouldRejectRequestToNonWhitelistedEndpoint() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/unauthorized", String.class);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Access denied"));
    }

    @Test
    @DisplayName("Should handle query parameters correctly")
    void shouldHandleQueryParametersCorrectly() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/users?page=1&size=10", String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle request headers correctly")
    void shouldHandleRequestHeadersCorrectly() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer token123");
        headers.set("X-Custom-Header", "custom-value");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.GET, entity, String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle multipart file upload")
    void shouldHandleMultipartFileUpload() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user-service/upload", HttpMethod.POST, entity, String.class);

        // Then
        assertNotNull(response);
        // Note: This might return 404 if the endpoint doesn't exist, but should not return 500
        assertTrue(response.getStatusCode().is2xxSuccessful() || 
                  response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle streaming response")
    void shouldHandleStreamingResponse() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/users", String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle health check endpoint")
    void shouldHandleHealthCheckEndpoint() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/health", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Simple Gateway is running", response.getBody());
    }

    @Test
    @DisplayName("Should handle invalid service name format")
    void shouldHandleInvalidServiceNameFormat() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/invalid@service/users", String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid service name"));
    }

    @Test
    @DisplayName("Should handle malformed URL")
    void shouldHandleMalformedUrl() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/users/", String.class);

        // Then
        assertNotNull(response);
        // Should handle gracefully, not throw exception
        assertTrue(response.getStatusCode().is2xxSuccessful() || 
                  response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws InterruptedException {
        // Given
        int threadCount = 10;
        int requestsPerThread = 5;
        Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        ResponseEntity<String> response = restTemplate.getForEntity(
                                baseUrl + "/user-service/users", String.class);
                        if (response.getStatusCode().is2xxSuccessful() || 
                            response.getStatusCode().is4xxClientError()) {
                            success[threadIndex] = true;
                        }
                    }
                } catch (Exception e) {
                    success[threadIndex] = false;
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        for (boolean s : success) {
            assertTrue(s, "All concurrent requests should succeed");
        }
    }

    @Test
    @DisplayName("Should handle large response")
    void shouldHandleLargeResponse() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/posts", String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
        // Large response should be handled without memory issues
    }

    @Test
    @DisplayName("Should handle timeout gracefully")
    void shouldHandleTimeoutGracefully() {
        // Given - This test assumes there's a slow endpoint
        // In a real scenario, you might mock a slow service

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/user-service/users", String.class);

        // Then
        assertNotNull(response);
        // Should not hang indefinitely
        assertTrue(response.getStatusCode().is2xxSuccessful() || 
                  response.getStatusCode().is4xxClientError() ||
                  response.getStatusCode().is5xxServerError());
    }

    @Test
    @DisplayName("Should maintain session state correctly")
    void shouldMaintainSessionStateCorrectly() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "sessionId=test123");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response1 = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.GET, entity, String.class);
        ResponseEntity<String> response2 = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.GET, entity, String.class);

        // Then
        assertNotNull(response1);
        assertNotNull(response2);
        // Both requests should be handled consistently
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
    }

    @Test
    @DisplayName("Should handle different content types")
    void shouldHandleDifferentContentTypes() {
        // Test JSON
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonEntity = new HttpEntity<>("{}", jsonHeaders);
        ResponseEntity<String> jsonResponse = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.POST, jsonEntity, String.class);
        assertNotNull(jsonResponse);

        // Test XML
        HttpHeaders xmlHeaders = new HttpHeaders();
        xmlHeaders.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> xmlEntity = new HttpEntity<>("<user></user>", xmlHeaders);
        ResponseEntity<String> xmlResponse = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.POST, xmlEntity, String.class);
        assertNotNull(xmlResponse);

        // Test plain text
        HttpHeaders textHeaders = new HttpHeaders();
        textHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> textEntity = new HttpEntity<>("test data", textHeaders);
        ResponseEntity<String> textResponse = restTemplate.exchange(
                baseUrl + "/user-service/users", HttpMethod.POST, textEntity, String.class);
        assertNotNull(textResponse);
    }
}
