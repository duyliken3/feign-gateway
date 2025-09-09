#!/bin/bash

echo "Testing Universal Feign Gateway API..."
echo "====================================="

# Wait for the application to start
echo "Waiting for application to start..."
sleep 10

# Test health endpoint
echo "1. Testing health endpoint..."
curl -s http://localhost:8080/api/health | jq .
echo ""

# Test get all users (from JSONPlaceholder)
echo "2. Testing get all users..."
curl -s http://localhost:8080/api/users | jq .
echo ""

# Test get user by ID
echo "3. Testing get user by ID..."
curl -s http://localhost:8080/api/users/1 | jq .
echo ""

# Test get all posts (from JSONPlaceholder)
echo "4. Testing get all posts..."
curl -s http://localhost:8080/api/posts | jq .
echo ""

# Test get post by ID
echo "5. Testing get post by ID..."
curl -s http://localhost:8080/api/posts/1 | jq .
echo ""

# Test get all comments (from JSONPlaceholder)
echo "6. Testing get all comments..."
curl -s http://localhost:8080/api/comments | jq .
echo ""

# Test get comment by ID
echo "7. Testing get comment by ID..."
curl -s http://localhost:8080/api/comments/1 | jq .
echo ""

# Test create post (to JSONPlaceholder)
echo "8. Testing create post..."
curl -s -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Post","body":"This is a test post","userId":1}' | jq .
echo ""

echo "Universal API testing completed!"
