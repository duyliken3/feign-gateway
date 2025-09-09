package com.example.feigngateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Simple Gateway is running", response.getBody());
    }

    @Test
    public void testGatewayRouting() {
        // Test that the gateway can handle requests
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/user-service/users/1", String.class);
        assertNotNull(response);
        // The response might be 200 or 404 depending on the external service, but should not be 500
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    public void testGatewayRoutingWithQueryParams() {
        // Test that the gateway properly handles query parameters
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/user-service/users?userId=1&name=John", String.class);
        assertNotNull(response);
        // The response might be 200 or 404 depending on the external service, but should not be 500
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }

    @Test
    public void testWhitelistAccessDenied() {
        // Test that non-whitelisted paths are denied
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/unknown-service/unauthorized", String.class);
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Access denied"));
    }
}
