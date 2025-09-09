# Performance Tuning Guide

## Overview

This guide provides comprehensive performance tuning recommendations for the Feign Gateway project to achieve optimal scalability and throughput.

## JVM Tuning

### Production JVM Settings

```bash
# Memory settings
-Xms2g -Xmx4g

# Garbage Collection (G1GC recommended)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat

# Performance optimizations
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1

# Monitoring and debugging
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:gc.log
```

### Development JVM Settings

```bash
# Smaller heap for development
-Xms512m -Xmx1g

# Faster startup
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1
-XX:+UseStringDeduplication
```

## Application Configuration

### Connection Pool Settings

```yaml
gateway:
  performance:
    connection-pool:
      max-total: 500          # Total connections
      max-per-route: 100      # Per service connections
      validate-after-inactivity: 2000  # Connection validation
      keep-alive-time: 30     # Keep-alive duration
```

### Thread Pool Settings

```yaml
gateway:
  performance:
    thread-pool:
      core-size: 20           # Core threads
      max-size: 100           # Maximum threads
      queue-capacity: 500     # Queue size
      keep-alive-seconds: 60  # Thread keep-alive
```

### Caching Settings

```yaml
gateway:
  performance:
    cache:
      enabled: true
      ttl: 300                # 5 minutes TTL
      max-size: 1000          # Maximum cache entries
```

## Performance Monitoring

### Key Metrics to Monitor

1. **Throughput**
   - Requests per second
   - Bytes transferred per second
   - Concurrent connections

2. **Latency**
   - Average response time
   - 95th percentile response time
   - 99th percentile response time

3. **Error Rates**
   - HTTP error rates by status code
   - Circuit breaker trip rates
   - Timeout rates

4. **Resource Utilization**
   - CPU usage
   - Memory usage
   - Thread pool utilization
   - Connection pool utilization

### Monitoring Endpoints

- `GET /api/performance/stats` - Overall statistics
- `GET /api/performance/stats/service/{service}` - Service-specific stats
- `GET /api/performance/circuit-breakers` - Circuit breaker status
- `GET /api/performance/cache` - Cache statistics
- `GET /actuator/metrics` - Micrometer metrics
- `GET /actuator/health` - Health status

## Load Testing

### Apache Bench (ab) Example

```bash
# Basic load test
ab -n 10000 -c 100 http://localhost:8080/api/execution/user-service/users

# Extended load test
ab -n 50000 -c 200 -t 60 http://localhost:8080/api/execution/user-service/users
```

### JMeter Test Plan

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan testname="Gateway Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

## Scaling Strategies

### Horizontal Scaling

1. **Load Balancer Configuration**
   ```nginx
   upstream feign_gateway {
       server gateway-1:8080 weight=1 max_fails=3 fail_timeout=30s;
       server gateway-2:8080 weight=1 max_fails=3 fail_timeout=30s;
       server gateway-3:8080 weight=1 max_fails=3 fail_timeout=30s;
   }
   ```

2. **Kubernetes Horizontal Pod Autoscaler**
   ```yaml
   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: feign-gateway-hpa
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: feign-gateway
     minReplicas: 3
     maxReplicas: 20
     metrics:
     - type: Resource
       resource:
         name: cpu
         target:
           type: Utilization
           averageUtilization: 70
   ```

### Vertical Scaling

1. **Memory Scaling**
   - Start with 2GB heap
   - Scale up to 8GB based on load
   - Monitor GC performance

2. **CPU Scaling**
   - Use multi-core systems
   - Configure thread pools appropriately
   - Monitor CPU utilization

## Performance Best Practices

### Code Optimizations

1. **Use Connection Pooling**
   - Configure appropriate pool sizes
   - Monitor connection usage
   - Implement connection validation

2. **Implement Caching**
   - Cache whitelist validations
   - Cache service configurations
   - Use appropriate TTL values

3. **Async Processing**
   - Use async for non-blocking operations
   - Configure appropriate thread pools
   - Monitor thread pool utilization

4. **Circuit Breaker Pattern**
   - Implement fault tolerance
   - Configure appropriate thresholds
   - Monitor circuit breaker states

### Database Optimizations

1. **Connection Pooling**
   - Use HikariCP for database connections
   - Configure appropriate pool sizes
   - Monitor connection usage

2. **Query Optimization**
   - Use prepared statements
   - Implement query caching
   - Monitor slow queries

### Network Optimizations

1. **HTTP/2 Support**
   - Enable HTTP/2 for better multiplexing
   - Configure appropriate connection limits
   - Monitor connection usage

2. **Compression**
   - Enable gzip compression
   - Configure appropriate compression levels
   - Monitor compression ratios

## Troubleshooting Performance Issues

### Common Issues

1. **High Memory Usage**
   - Check for memory leaks
   - Monitor GC performance
   - Adjust heap size

2. **High CPU Usage**
   - Check for infinite loops
   - Monitor thread usage
   - Optimize algorithms

3. **Slow Response Times**
   - Check network latency
   - Monitor downstream services
   - Optimize database queries

4. **Connection Pool Exhaustion**
   - Increase pool sizes
   - Check for connection leaks
   - Monitor connection usage

### Performance Profiling

1. **JProfiler**
   - CPU profiling
   - Memory profiling
   - Thread profiling

2. **VisualVM**
   - Memory monitoring
   - Thread monitoring
   - GC monitoring

3. **Application Performance Monitoring (APM)**
   - New Relic
   - DataDog
   - AppDynamics

## Performance Testing Checklist

- [ ] Load testing with realistic data
- [ ] Stress testing beyond normal capacity
- [ ] Endurance testing for memory leaks
- [ ] Spike testing for sudden load increases
- [ ] Volume testing with large datasets
- [ ] Scalability testing with multiple instances
- [ ] Performance regression testing
- [ ] Monitoring and alerting setup

## Expected Performance Targets

### Throughput
- **Target**: 10,000+ requests per second
- **Peak**: 20,000+ requests per second
- **Sustained**: 5,000+ requests per second

### Latency
- **P50**: < 50ms
- **P95**: < 200ms
- **P99**: < 500ms

### Availability
- **Target**: 99.9% uptime
- **MTTR**: < 5 minutes
- **MTBF**: > 30 days

### Resource Utilization
- **CPU**: < 70% average
- **Memory**: < 80% of allocated heap
- **Network**: < 80% of bandwidth
