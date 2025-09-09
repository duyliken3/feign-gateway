# Feign Gateway Deployment Guide

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development](#local-development)
3. [Docker Deployment](#docker-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Production Deployment](#production-deployment)
6. [Configuration Management](#configuration-management)
7. [Monitoring & Logging](#monitoring--logging)
8. [Security Considerations](#security-considerations)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Java 21** or higher
- **Maven 3.6** or higher
- **Docker** (for containerized deployment)
- **Kubernetes** (for K8s deployment)
- **Redis** (for rate limiting - optional)

### External Dependencies

- Target microservices must be accessible
- Network connectivity to external APIs
- Proper DNS resolution

## Local Development

### 1. Clone the Repository

```bash
git clone <repository-url>
cd feign_gateway
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Using the provided script
./run.sh

# Using JAR file
java -jar target/feign-gateway-1.0.0.jar
```

### 4. Verify Deployment

```bash
# Health check
curl http://localhost:8080/api/execution/health

# Test API
./test-api.sh
```

## Docker Deployment

### 1. Create Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Copy the JAR file
COPY target/feign-gateway-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build Docker Image

```bash
docker build -t feign-gateway:latest .
```

### 3. Run Docker Container

```bash
# Basic run
docker run -p 8080:8080 feign-gateway:latest

# With environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e GATEWAY_WHITELIST_SERVICES_0_NAME=user-service \
  -e GATEWAY_WHITELIST_SERVICES_0_BASE_URL=https://api.example.com \
  feign-gateway:latest
```

### 4. Docker Compose

```yaml
version: '3.8'

services:
  feign-gateway:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - redis
    networks:
      - gateway-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - gateway-network

networks:
  gateway-network:
    driver: bridge
```

## Kubernetes Deployment

### 1. Create Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: feign-gateway
```

### 2. Create ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: feign-gateway-config
  namespace: feign-gateway
data:
  application.yml: |
    server:
      port: 8080
    
    spring:
      application:
        name: feign-gateway
      cloud:
        loadbalancer:
          enabled: true
    
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
```

### 3. Create Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: feign-gateway
  namespace: feign-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: feign-gateway
  template:
    metadata:
      labels:
        app: feign-gateway
    spec:
      containers:
      - name: feign-gateway
        image: feign-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/execution/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/execution/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        volumeMounts:
        - name: config
          mountPath: /app/config
      volumes:
      - name: config
        configMap:
          name: feign-gateway-config
```

### 4. Create Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: feign-gateway-service
  namespace: feign-gateway
spec:
  selector:
    app: feign-gateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 5. Create Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: feign-gateway-ingress
  namespace: feign-gateway
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: feign-gateway-service
            port:
              number: 80
```

### 6. Deploy to Kubernetes

```bash
# Apply all resources
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n feign-gateway

# Check service
kubectl get svc -n feign-gateway

# Check ingress
kubectl get ingress -n feign-gateway
```

## Production Deployment

### 1. Environment Setup

```bash
# Create production environment
export SPRING_PROFILES_ACTIVE=prod
export GATEWAY_WHITELIST_ENABLED=true
export GATEWAY_WHITELIST_SERVICES_0_NAME=user-service
export GATEWAY_WHITELIST_SERVICES_0_BASE_URL=https://api.example.com
```

### 2. JVM Tuning

```bash
# Production JVM options
java -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -jar feign-gateway-1.0.0.jar
```

### 3. Load Balancer Configuration

```nginx
upstream feign_gateway {
    server feign-gateway-1:8080;
    server feign-gateway-2:8080;
    server feign-gateway-3:8080;
}

server {
    listen 80;
    server_name api.example.com;
    
    location / {
        proxy_pass http://feign_gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Configuration Management

### 1. Environment-Specific Configuration

#### Development
```yaml
# application-dev.yml
server:
  port: 8080

logging:
  level:
    com.example.feigngateway: DEBUG
```

#### Production
```yaml
# application-prod.yml
server:
  port: 8080

logging:
  level:
    com.example.feigngateway: INFO
    root: WARN

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 2. External Configuration

```yaml
# application.yml
spring:
  config:
    import: optional:configserver:http://config-server:8888
```

### 3. Secrets Management

```yaml
# secrets.yml
apiVersion: v1
kind: Secret
metadata:
  name: feign-gateway-secrets
type: Opaque
data:
  api-key: <base64-encoded-api-key>
  database-password: <base64-encoded-password>
```

## Monitoring & Logging

### 1. Application Metrics

```yaml
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. Logging Configuration

```yaml
# logback-spring.xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/feign-gateway.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/feign-gateway.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 3. Prometheus Monitoring

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'feign-gateway'
    static_configs:
      - targets: ['feign-gateway:8080']
    metrics_path: '/actuator/prometheus'
```

## Security Considerations

### 1. Network Security

- Use HTTPS in production
- Implement proper firewall rules
- Use VPN for internal communication
- Enable DDoS protection

### 2. Application Security

- Implement authentication and authorization
- Use rate limiting
- Validate all inputs
- Implement proper error handling
- Use secure headers

### 3. Container Security

- Use non-root user
- Scan images for vulnerabilities
- Keep base images updated
- Use minimal base images

## Troubleshooting

### Common Issues

#### 1. Service Not Found (404)

**Problem:** Service not whitelisted
**Solution:** Add service to whitelist configuration

#### 2. Connection Timeout

**Problem:** Target service unavailable
**Solution:** Check target service status and network connectivity

#### 3. Memory Issues

**Problem:** OutOfMemoryError
**Solution:** Increase heap size and optimize GC settings

#### 4. Port Already in Use

**Problem:** Port 8080 already occupied
**Solution:** Change port or stop conflicting service

### Debug Commands

```bash
# Check application logs
kubectl logs -f deployment/feign-gateway -n feign-gateway

# Check service status
kubectl get svc -n feign-gateway

# Check pod status
kubectl get pods -n feign-gateway

# Check ingress status
kubectl get ingress -n feign-gateway

# Port forward for local testing
kubectl port-forward svc/feign-gateway-service 8080:80 -n feign-gateway
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/api/execution/health

# Kubernetes health
kubectl get pods -n feign-gateway

# Service health
kubectl get svc -n feign-gateway
```

## Performance Tuning

### 1. JVM Tuning

```bash
# G1GC with optimized settings
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+UseStringDeduplication
```

### 2. Connection Pooling

```yaml
# application.yml
feign:
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50
    connection-timeout: 5000
    read-timeout: 10000
```

### 3. Caching

```yaml
# Add Redis caching
spring:
  data:
    redis:
      host: redis
      port: 6379
      database: 0
```

## Backup & Recovery

### 1. Configuration Backup

```bash
# Backup configuration
kubectl get configmap feign-gateway-config -n feign-gateway -o yaml > config-backup.yaml
```

### 2. Application Backup

```bash
# Backup application
kubectl get deployment feign-gateway -n feign-gateway -o yaml > deployment-backup.yaml
```

### 3. Recovery

```bash
# Restore configuration
kubectl apply -f config-backup.yaml

# Restore application
kubectl apply -f deployment-backup.yaml
```

## Scaling

### 1. Horizontal Scaling

```bash
# Scale deployment
kubectl scale deployment feign-gateway --replicas=5 -n feign-gateway
```

### 2. Auto-scaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: feign-gateway-hpa
  namespace: feign-gateway
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: feign-gateway
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## Maintenance

### 1. Rolling Updates

```bash
# Update image
kubectl set image deployment/feign-gateway feign-gateway=feign-gateway:v2.0.0 -n feign-gateway

# Check rollout status
kubectl rollout status deployment/feign-gateway -n feign-gateway
```

### 2. Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/feign-gateway -n feign-gateway
```

### 3. Cleanup

```bash
# Delete resources
kubectl delete namespace feign-gateway

# Clean up Docker images
docker rmi feign-gateway:latest
```
