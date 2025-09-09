# Universal Feign Gateway Service

A Spring Boot universal gateway service that receives requests through a single endpoint and automatically routes them to appropriate services using Feign client with whitelist configuration. This gateway provides a centralized entry point for microservices communication with advanced security, monitoring, and rate limiting capabilities.

## 🚀 Features

### Core Features
- **Universal Endpoint**: Single `/api/execution/**` endpoint handles all requests
- **Dynamic Routing**: Automatically routes requests based on path patterns
- **Feign Client Integration**: Uses Spring Cloud OpenFeign for service-to-service communication
- **Whitelist Security**: Configurable whitelist to control which services and endpoints are accessible
- **Load Balancing**: Built-in load balancing support with Spring Cloud Load Balancer
- **Request/Response Logging**: Comprehensive AOP-based logging for debugging and monitoring
- **Streaming Support**: Handles file uploads and streaming responses
- **Multipart Support**: Full multipart form data handling

### Security Features
- **Whitelist Validation**: Only whitelisted services and endpoints are accessible
- **Path Pattern Matching**: Ant-style pattern matching for flexible endpoint configuration
- **Request Validation**: Input validation and sanitization
- **Error Handling**: Comprehensive error handling with appropriate HTTP status codes

### Monitoring & Observability
- **Request Logging**: Detailed request/response logging with timing information
- **AOP Logging**: Aspect-oriented logging for all controller methods
- **Error Tracking**: Comprehensive error logging and tracking
- **Performance Metrics**: Request duration and performance monitoring

## 🏗️ Architecture

```
Client Request → Universal Gateway → Service Router → Feign Client → Target Service
                     ↓                    ↓
              Whitelist Validation   Path Pattern Matching
                     ↓
              Request Logging (AOP)
                     ↓
              Error Handling & Response
```

### Component Overview

- **SimpleGatewayController**: Main entry point handling all `/api/execution/**` requests
- **GatewayService**: Core routing logic with RestTemplate-based forwarding
- **WhitelistService**: Security validation and service discovery
- **RequestLoggingAspect**: AOP-based request/response logging
- **StreamingService**: Handles file streaming and large responses
- **GlobalExceptionHandler**: Centralized error handling

## ⚙️ Configuration

The gateway is configured via `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: feign-gateway
  cloud:
    loadbalancer:
      enabled: true

# Feign client configuration
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50

# Whitelist configuration for allowed services
gateway:
  whitelist:
    enabled: true
    services:
      - name: user-service
        base-url: https://jsonplaceholder.typicode.com
        endpoints:
          - /users/**
          - /users/{id}
      - name: post-service
        base-url: https://dummyjson.com
        endpoints:
          - /posts/**
          - /posts/{id}
      - name: comment-service
        base-url: https://dummyjson.com
        endpoints:
          - /comments/**
          - /comments/{id}

# Logging configuration
logging:
  level:
    com.example.feigngateway: DEBUG
    feign: DEBUG
    org.springframework.cloud.openfeign: DEBUG
```

## 📡 API Endpoints

### Universal Gateway Endpoints

All requests go through the universal gateway at `/api/execution/{service}/**`:

#### Request Format
```
/api/execution/{service-name}/{endpoint-path}
```

#### User Service
- `GET /api/execution/user-service/users` - Get all users
- `GET /api/execution/user-service/users/{id}` - Get user by ID
- `POST /api/execution/user-service/users` - Create new user
- `PUT /api/execution/user-service/users/{id}` - Update user
- `DELETE /api/execution/user-service/users/{id}` - Delete user

#### Post Service
- `GET /api/execution/post-service/posts` - Get all posts
- `GET /api/execution/post-service/posts/{id}` - Get post by ID
- `POST /api/execution/post-service/posts` - Create new post
- `PUT /api/execution/post-service/posts/{id}` - Update post
- `DELETE /api/execution/post-service/posts/{id}` - Delete post

#### Comment Service
- `GET /api/execution/comment-service/comments` - Get all comments
- `GET /api/execution/comment-service/comments/{id}` - Get comment by ID
- `POST /api/execution/comment-service/comments` - Create new comment
- `PUT /api/execution/comment-service/comments/{id}` - Update comment
- `DELETE /api/execution/comment-service/comments/{id}` - Delete comment

#### Special Endpoints
- `GET /api/execution/health` - Gateway health status
- `GET /api/execution/{service}/**` - Stream large responses (application/octet-stream)
- `POST /api/execution/{service}/**` - Multipart file uploads

## 🚀 Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- Internet connection (for external API calls)

### Quick Start

1. **Clone and build the project**:
   ```bash
   git clone <repository-url>
   cd feign_gateway
   mvn clean install
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   
   Or use the provided script:
   ```bash
   ./run.sh
   ```

3. **The gateway will start on port 8080**

4. **Test the gateway**:
   ```bash
   ./test-api.sh
   ```

5. **View demo**:
   ```bash
   ./demo.sh
   ```

## 🧪 Testing the Gateway

### Using curl

1. **Health check**:
   ```bash
   curl http://localhost:8080/api/execution/health
   ```

2. **Get all users**:
   ```bash
   curl http://localhost:8080/api/execution/user-service/users
   ```

3. **Get user by ID**:
   ```bash
   curl http://localhost:8080/api/execution/user-service/users/1
   ```

4. **Get all posts**:
   ```bash
   curl http://localhost:8080/api/execution/post-service/posts
   ```

5. **Get post by ID**:
   ```bash
   curl http://localhost:8080/api/execution/post-service/posts/1
   ```

6. **Get all comments**:
   ```bash
   curl http://localhost:8080/api/execution/comment-service/comments
   ```

7. **Test with query parameters**:
   ```bash
   curl "http://localhost:8080/api/execution/user-service/users?limit=10&offset=0"
   ```

8. **Test multipart upload**:
   ```bash
   curl -X POST http://localhost:8080/api/execution/user-service/upload \
     -F "file=@example.txt" \
     -F "description=Test upload"
   ```

### Using Postman

Import the following collection or create requests manually:

- **GET** `http://localhost:8080/api/execution/health`
- **GET** `http://localhost:8080/api/execution/user-service/users`
- **GET** `http://localhost:8080/api/execution/user-service/users/1`
- **POST** `http://localhost:8080/api/execution/user-service/users` (with JSON body)
- **GET** `http://localhost:8080/api/execution/post-service/posts`
- **GET** `http://localhost:8080/api/execution/comment-service/comments`

## Whitelist Configuration

The whitelist controls which services and endpoints are accessible through the gateway:

- **enabled**: Set to `true` to enable whitelist validation
- **services**: List of allowed services with their base URLs and permitted endpoints
- **endpoints**: Ant-style path patterns for allowed endpoints

### Example Whitelist Patterns

- `/api/users/**` - All user endpoints
- `/api/users/{id}` - Specific user endpoint
- `/api/orders/user/{userId}` - Orders by user endpoint

## Security Features

1. **Whitelist Validation**: Only whitelisted services and endpoints are accessible
2. **Request Logging**: All requests are logged for audit purposes
3. **Error Handling**: Comprehensive error handling with appropriate HTTP status codes
4. **Response Wrapping**: All responses are wrapped in a consistent `ApiResponse` format

## Error Responses

The gateway returns consistent error responses:

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "statusCode": 500
}
```

Common error scenarios:
- **403 Forbidden**: Service not whitelisted
- **500 Internal Server Error**: Service unavailable or error occurred
- **404 Not Found**: Resource not found

## Development Notes

- The application includes mock services for testing
- Feign clients are configured with timeouts and connection pooling
- All services use the same port (8080) for simplicity in this example
- In production, you would run each service on different ports

## 📦 Dependencies

### Core Dependencies
- **Spring Boot 3.2.0** - Main framework
- **Spring Cloud OpenFeign** - Service-to-service communication
- **Spring Cloud Load Balancer** - Load balancing support
- **Spring AOP** - Aspect-oriented programming for logging
- **Jackson** - JSON processing
- **Lombok** - Reducing boilerplate code

### Testing Dependencies
- **JUnit 4.13.2** - Unit testing framework
- **Mockito 4.11.0** - Mocking framework
- **Spring Boot Test** - Integration testing support

### Build Dependencies
- **Maven** - Build and dependency management
- **Spring Boot Maven Plugin** - Application packaging

## 🔧 Project Structure

```
feign_gateway/
├── src/main/java/com/example/feigngateway/
│   ├── aspect/
│   │   └── RequestLoggingAspect.java          # AOP logging
│   ├── config/
│   │   ├── GatewayWhitelistProperties.java    # Configuration properties
│   │   └── RestTemplateConfig.java            # RestTemplate configuration
│   ├── controller/
│   │   └── SimpleGatewayController.java       # Main gateway controller
│   ├── exception/
│   │   ├── GatewayException.java              # Custom exceptions
│   │   └── GlobalExceptionHandler.java        # Global error handling
│   ├── service/
│   │   ├── GatewayService.java                # Core routing logic
│   │   ├── StreamingService.java              # File streaming
│   │   └── WhitelistService.java              # Security validation
│   └── FeignGatewayApplication.java           # Main application class
├── src/main/resources/
│   └── application.yml                        # Configuration
├── src/test/java/                            # Test classes
├── pom.xml                                   # Maven configuration
├── run.sh                                    # Run script
├── test-api.sh                               # Test script
├── demo.sh                                   # Demo script
└── README.md                                 # This file
```

## 🚀 Future Enhancements

### Planned Features
- **Redis-based Rate Limiting** - Distributed rate limiting
- **JWT Authentication** - Token-based authentication
- **Circuit Breaker Pattern** - Fault tolerance
- **Distributed Tracing** - Request tracing across services
- **Metrics & Monitoring** - Prometheus/Grafana integration
- **API Documentation** - OpenAPI/Swagger integration
- **Caching Strategy** - Response caching with Redis
- **Health Checks** - Advanced health monitoring

### Security Improvements
- **OAuth2 Integration** - OAuth2 resource server
- **API Key Authentication** - API key-based auth
- **Request Signing** - HMAC request signing
- **CORS Configuration** - Cross-origin resource sharing
- **Security Headers** - Security header implementation

### Performance Optimizations
- **HTTP/2 Support** - HTTP/2 protocol support
- **Connection Pooling** - Optimized connection management
- **Async Processing** - Non-blocking request processing
- **CDN Integration** - Content delivery network

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation and examples

## 📚 Documentation

- **[API Documentation](API_DOCUMENTATION.md)** - Complete API reference and examples
- **[Architecture Guide](ARCHITECTURE.md)** - System architecture and design patterns
- **[Deployment Guide](DEPLOYMENT_GUIDE.md)** - Deployment strategies and configurations
- **[Testing Guide](TESTING_GUIDE.md)** - Comprehensive testing strategies
- **[Universal Gateway](UNIVERSAL_GATEWAY.md)** - Detailed feature overview
- **[Changelog](CHANGELOG.md)** - Version history and changes

## 🏷️ Version History

- **v1.0.0** - Initial release with basic gateway functionality
- **v1.1.0** - Added AOP logging and streaming support
- **v1.2.0** - Enhanced error handling and multipart support
- **v1.3.0** - Improved configuration and documentation
- **v1.4.0** - Comprehensive documentation and architecture guides
