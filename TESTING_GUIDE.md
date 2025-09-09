# Feign Gateway Testing Guide

## Overview

This guide covers comprehensive testing strategies for the Feign Gateway project, including unit tests, integration tests, and end-to-end testing.

## Test Structure

```
src/test/java/com/example/feigngateway/
├── GatewayServiceTest.java              # Integration tests
├── service/
│   └── SimpleUnitTests.java            # Unit tests
├── exception/
│   └── ExceptionTests.java             # Exception handling tests
└── aspect/
    └── RequestLoggingAspectTest.java   # AOP tests
```

## Unit Testing

### 1. Service Layer Tests

```java
@RunWith(SpringRunner.class)
@SpringBootTest
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
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/user-service/users/1", String.class);
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
    }
}
```

### 2. Whitelist Service Tests

```java
public class WhitelistServiceTest {
    
    private WhitelistService whitelistService;
    private GatewayWhitelistProperties properties;
    
    @Before
    public void setUp() {
        properties = new GatewayWhitelistProperties();
        whitelistService = new WhitelistService(properties);
    }
    
    @Test
    public void testWhitelistDisabled() {
        properties.setEnabled(false);
        assertTrue(whitelistService.isRequestAllowed("any-service", "/any-path"));
    }
    
    @Test
    public void testWhitelistEnabled() {
        properties.setEnabled(true);
        GatewayWhitelistProperties.ServiceConfig serviceConfig = new GatewayWhitelistProperties.ServiceConfig();
        serviceConfig.setName("user-service");
        serviceConfig.setBaseUrl("https://jsonplaceholder.typicode.com");
        serviceConfig.setEndpoints(Arrays.asList("/users/**"));
        properties.setServices(Arrays.asList(serviceConfig));
        
        assertTrue(whitelistService.isRequestAllowed("user-service", "/users/1"));
        assertFalse(whitelistService.isRequestAllowed("user-service", "/unauthorized"));
    }
}
```

## Integration Testing

### 1. Controller Integration Tests

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testGetUsers() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/user-service/users", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testGetUserById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/user-service/users/1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("id"));
    }
}
```

### 2. Error Handling Tests

```java
@Test
public void testWhitelistAccessDenied() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/execution/unknown-service/unauthorized", String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(response.getBody().contains("Access denied"));
}
```

## Performance Testing

### 1. Load Testing with JMeter

```xml
<!-- JMeter Test Plan -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan testname="Feign Gateway Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

### 2. Stress Testing

```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/execution/health

# Using wrk
wrk -t12 -c400 -d30s http://localhost:8080/api/execution/user-service/users
```

## End-to-End Testing

### 1. API Testing Scripts

```bash
#!/bin/bash
# test-api.sh

echo "Testing Feign Gateway API..."

# Health check
echo "1. Testing health endpoint..."
curl -s http://localhost:8080/api/execution/health
echo ""

# User service tests
echo "2. Testing user service..."
curl -s http://localhost:8080/api/execution/user-service/users | head -c 100
echo ""

# Post service tests
echo "3. Testing post service..."
curl -s http://localhost:8080/api/execution/post-service/posts | head -c 100
echo ""

echo "API testing completed!"
```

### 2. Postman Collection

```json
{
  "info": {
    "name": "Feign Gateway API Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/execution/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "execution", "health"]
        }
      }
    },
    {
      "name": "Get Users",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/execution/user-service/users",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "execution", "user-service", "users"]
        }
      }
    }
  ]
}
```

## Test Configuration

### 1. Test Properties

```yaml
# application-test.yml
server:
  port: 0

spring:
  profiles:
    active: test

gateway:
  whitelist:
    enabled: true
    services:
      - name: user-service
        base-url: https://jsonplaceholder.typicode.com
        endpoints:
          - /users/**
      - name: post-service
        base-url: https://dummyjson.com
        endpoints:
          - /posts/**

logging:
  level:
    com.example.feigngateway: DEBUG
```

### 2. Test Dependencies

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit4</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>redis</artifactId>
    <scope>test</scope>
</dependency>
```

## Continuous Integration

### 1. GitHub Actions

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn verify
```

### 2. Test Reports

```xml
<!-- Add to pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Test Data Management

### 1. Test Data Setup

```java
@Component
public class TestDataSetup {
    
    @PostConstruct
    public void setupTestData() {
        // Setup test data for integration tests
    }
    
    @PreDestroy
    public void cleanupTestData() {
        // Cleanup test data after tests
    }
}
```

### 2. Mock Services

```java
@RestController
@RequestMapping("/mock")
public class MockServiceController {
    
    @GetMapping("/users")
    public List<User> getUsers() {
        return Arrays.asList(
            new User(1, "John Doe", "john@example.com"),
            new User(2, "Jane Smith", "jane@example.com")
        );
    }
}
```

## Testing Best Practices

### 1. Test Naming

- Use descriptive test names
- Follow the pattern: `methodName_condition_expectedResult`
- Example: `isRequestAllowed_whenServiceNotWhitelisted_shouldReturnFalse`

### 2. Test Organization

- Group related tests in the same class
- Use `@Before` and `@After` methods for setup/cleanup
- Keep tests independent and isolated

### 3. Assertions

- Use specific assertions
- Test both positive and negative cases
- Verify error messages and status codes

### 4. Test Coverage

- Aim for high test coverage (>80%)
- Focus on critical business logic
- Test edge cases and error conditions

## Running Tests

### 1. Unit Tests

```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=WhitelistServiceTest

# Run with coverage
mvn test jacoco:report
```

### 2. Integration Tests

```bash
# Run integration tests
mvn verify

# Run with specific profile
mvn verify -Dspring.profiles.active=test
```

### 3. All Tests

```bash
# Run all tests
mvn clean test verify

# Run with coverage report
mvn clean test verify jacoco:report
```

## Test Monitoring

### 1. Test Metrics

- Test execution time
- Test success rate
- Code coverage percentage
- Flaky test detection

### 2. Test Reporting

- Generate HTML reports
- Integrate with CI/CD
- Track test trends over time
- Alert on test failures

## Troubleshooting

### Common Test Issues

1. **Port conflicts**: Use random ports for tests
2. **External dependencies**: Mock external services
3. **Test data**: Use test-specific data
4. **Timing issues**: Use proper waits and timeouts

### Debug Tips

1. Enable debug logging
2. Use breakpoints in tests
3. Check test output and logs
4. Verify test environment setup
