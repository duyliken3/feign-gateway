# Feign Gateway API Documentation

## Overview

The Feign Gateway provides a universal API gateway that routes requests to various microservices through a single endpoint. All requests are processed through the `/api/execution/{service}/**` pattern.

## 🆕 **New Features (v1.5.0)**

### **Performance Monitoring Endpoints**
- **`GET /api/performance/stats`** - Overall performance statistics
- **`GET /api/performance/stats/service/{service}`** - Service-specific metrics
- **`GET /api/performance/circuit-breakers`** - Circuit breaker status
- **`GET /api/performance/cache`** - Cache statistics
- **`GET /api/performance/health`** - Performance health status

### **Enhanced Error Handling**
- Structured error responses with correlation IDs
- Comprehensive validation with detailed error messages
- Proper HTTP status codes for different error types

### **Structured Logging**
- Correlation IDs for request tracking
- MDC-based contextual logging
- Performance metrics integration

## Base URL

```
http://localhost:8080
```

## Authentication

Currently, the gateway does not require authentication. Future versions will support JWT tokens, API keys, and OAuth2.

## Request Format

All requests follow this pattern:
```
/api/execution/{service-name}/{endpoint-path}
```

### Path Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| service-name | string | The name of the target service | user-service |
| endpoint-path | string | The path within the target service | users/1 |

### Query Parameters

Query parameters are automatically forwarded to the target service.

### Request Headers

| Header | Type | Required | Description |
|--------|------|----------|-------------|
| Content-Type | string | No | Content type of the request body |
| Accept | string | No | Expected response content type |
| User-Agent | string | No | Client user agent |

## Response Format

### Success Response

```json
{
  "success": true,
  "data": { ... },
  "message": "Success",
  "statusCode": 200
}
```

### Error Response

```json
{
  "success": false,
  "data": null,
  "message": "Error message",
  "statusCode": 400
}
```

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request |
| 403 | Forbidden (Service not whitelisted) |
| 404 | Not Found |
| 500 | Internal Server Error |

## Endpoints

### Health Check

#### GET /api/execution/health

Check the gateway health status.

**Response:**
```
Simple Gateway is running
```

### User Service

#### GET /api/execution/user-service/users

Get all users.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Leanne Graham",
    "username": "Bret",
    "email": "Sincere@april.biz",
    "phone": "1-770-736-8031 x56442",
    "website": "hildegard.org"
  }
]
```

#### GET /api/execution/user-service/users/{id}

Get a specific user by ID.

**Path Parameters:**
- `id` (integer): User ID

**Response:**
```json
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org"
}
```

#### POST /api/execution/user-service/users

Create a new user.

**Request Body:**
```json
{
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "phone": "1-555-123-4567",
  "website": "johndoe.com"
}
```

**Response:**
```json
{
  "id": 11,
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "phone": "1-555-123-4567",
  "website": "johndoe.com"
}
```

#### PUT /api/execution/user-service/users/{id}

Update an existing user.

**Path Parameters:**
- `id` (integer): User ID

**Request Body:**
```json
{
  "name": "John Doe Updated",
  "username": "johndoe",
  "email": "john.updated@example.com",
  "phone": "1-555-123-4567",
  "website": "johndoe.com"
}
```

#### DELETE /api/execution/user-service/users/{id}

Delete a user.

**Path Parameters:**
- `id` (integer): User ID

**Response:**
```
200 OK
```

### Post Service

#### GET /api/execution/post-service/posts

Get all posts.

**Response:**
```json
{
  "posts": [
    {
      "id": 1,
      "title": "Post Title",
      "body": "Post content...",
      "userId": 1,
      "tags": ["tag1", "tag2"],
      "reactions": 5
    }
  ],
  "total": 150,
  "skip": 0,
  "limit": 30
}
```

#### GET /api/execution/post-service/posts/{id}

Get a specific post by ID.

**Path Parameters:**
- `id` (integer): Post ID

**Response:**
```json
{
  "id": 1,
  "title": "Post Title",
  "body": "Post content...",
  "userId": 1,
  "tags": ["tag1", "tag2"],
  "reactions": 5
}
```

### Comment Service

#### GET /api/execution/comment-service/comments

Get all comments.

**Response:**
```json
{
  "comments": [
    {
      "id": 1,
      "body": "Comment content...",
      "postId": 1,
      "user": {
        "id": 1,
        "username": "user1"
      }
    }
  ],
  "total": 340,
  "skip": 0,
  "limit": 30
}
```

#### GET /api/execution/comment-service/comments/{id}

Get a specific comment by ID.

**Path Parameters:**
- `id` (integer): Comment ID

**Response:**
```json
{
  "id": 1,
  "body": "Comment content...",
  "postId": 1,
  "user": {
    "id": 1,
    "username": "user1"
  }
}
```

## Special Endpoints

### Streaming Responses

#### GET /api/execution/{service}/** (with Accept: application/octet-stream)

Stream large responses for file downloads or large data sets.

**Headers:**
- `Accept: application/octet-stream`

### Multipart Uploads

#### POST /api/execution/{service}/** (with Content-Type: multipart/form-data)

Upload files or form data.

**Request Body:**
```
Content-Type: multipart/form-data

file: [binary data]
description: "File description"
```

## Error Handling

### Common Error Scenarios

#### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied: Path not whitelisted",
  "data": null,
  "statusCode": 403
}
```

#### 404 Not Found
```json
{
  "success": false,
  "message": "Service not found",
  "data": null,
  "statusCode": 404
}
```

#### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error: Connection timeout",
  "data": null,
  "statusCode": 500
}
```

## Rate Limiting

Currently, rate limiting is not implemented. Future versions will include:
- Global rate limiting
- Service-specific rate limiting
- User-based rate limiting
- Redis-based distributed rate limiting

## CORS

CORS is not currently configured. Future versions will include:
- Configurable CORS policies
- Preflight request handling
- Credential support

## Examples

### cURL Examples

#### Get all users
```bash
curl -X GET "http://localhost:8080/api/execution/user-service/users"
```

#### Get user by ID
```bash
curl -X GET "http://localhost:8080/api/execution/user-service/users/1"
```

#### Create a new user
```bash
curl -X POST "http://localhost:8080/api/execution/user-service/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "phone": "1-555-123-4567",
    "website": "johndoe.com"
  }'
```

#### Update a user
```bash
curl -X PUT "http://localhost:8080/api/execution/user-service/users/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "username": "johndoe",
    "email": "john.updated@example.com",
    "phone": "1-555-123-4567",
    "website": "johndoe.com"
  }'
```

#### Delete a user
```bash
curl -X DELETE "http://localhost:8080/api/execution/user-service/users/1"
```

#### Upload a file
```bash
curl -X POST "http://localhost:8080/api/execution/user-service/upload" \
  -F "file=@example.txt" \
  -F "description=Test upload"
```

### JavaScript Examples

#### Fetch all users
```javascript
fetch('http://localhost:8080/api/execution/user-service/users')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

#### Create a new user
```javascript
fetch('http://localhost:8080/api/execution/user-service/users', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    name: 'John Doe',
    username: 'johndoe',
    email: 'john@example.com',
    phone: '1-555-123-4567',
    website: 'johndoe.com'
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

## Configuration

The gateway behavior can be configured through `application.yml`:

```yaml
gateway:
  whitelist:
    enabled: true
    services:
      - name: user-service
        base-url: https://jsonplaceholder.typicode.com
        endpoints:
          - /users/**
          - /users/{id}
```

## Monitoring

The gateway provides comprehensive logging for all requests:

- Request/response logging
- Performance metrics
- Error tracking
- Debug information

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation and examples
