package com.example.feigngateway.aspect;

import com.example.feigngateway.service.PerformanceMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StructuredLoggingAspect {
    
    private final PerformanceMetricsService metricsService;
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String SERVICE_NAME_KEY = "serviceName";
    private static final String METHOD_KEY = "method";
    private static final String PATH_KEY = "path";
    
    @Pointcut("execution(* com.example.feigngateway.controller.*.*(..))")
    public void controllerMethods() {}
    
    @Around("controllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        String correlationId = generateCorrelationId();
        String requestId = generateRequestId();
        
        // Set MDC for structured logging
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(REQUEST_ID_KEY, requestId);
        
        if (request != null) {
            long startTime = System.currentTimeMillis();
            
            // Extract service name from path
            String serviceName = extractServiceName(request.getRequestURI());
            MDC.put(SERVICE_NAME_KEY, serviceName);
            MDC.put(METHOD_KEY, request.getMethod());
            MDC.put(PATH_KEY, request.getRequestURI());
            
            // Log structured request data
            logStructuredRequest(request, joinPoint, correlationId, requestId);
            
            try {
                Object result = joinPoint.proceed();
                
                long duration = System.currentTimeMillis() - startTime;
                logStructuredResponse(result, duration, true, null);
                
                // Record metrics
                recordMetrics(serviceName, duration, true);
                
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                logStructuredResponse(null, duration, false, e);
                
                // Record error metrics
                recordMetrics(serviceName, duration, false);
                
                throw e;
            } finally {
                // Clean up MDC
                clearMDC();
            }
        }
        
        return joinPoint.proceed();
    }
    
    private void logStructuredRequest(HttpServletRequest request, ProceedingJoinPoint joinPoint, 
                                   String correlationId, String requestId) {
        log.info("""
            Request received - 
            correlationId: {}, requestId: {}, method: {}, uri: {}, 
            queryString: {}, remoteAddr: {}, userAgent: {}, 
            contentType: {}, contentLength: {}, controller: {}.{}, 
            args: {}""",
            correlationId, requestId, request.getMethod(), request.getRequestURI(),
            request.getQueryString(), request.getRemoteAddr(), request.getHeader("User-Agent"),
            request.getContentType(), request.getContentLength(),
            joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getSignature().getName(),
            Arrays.toString(joinPoint.getArgs()));
    }
    
    private void logStructuredResponse(Object result, long duration, boolean success, Exception error) {
        if (success) {
            log.info("""
                Request completed - 
                correlationId: {}, requestId: {}, duration: {}ms, 
                status: SUCCESS, responseType: {}""",
                MDC.get(CORRELATION_ID_KEY), MDC.get(REQUEST_ID_KEY), duration,
                result != null ? result.getClass().getSimpleName() : "null");
        } else {
            log.error("""
                Request failed - 
                correlationId: {}, requestId: {}, duration: {}ms, 
                status: ERROR, errorType: {}, errorMessage: {}""",
                MDC.get(CORRELATION_ID_KEY), MDC.get(REQUEST_ID_KEY), duration,
                error != null ? error.getClass().getSimpleName() : "Unknown",
                error != null ? error.getMessage() : "Unknown error", error);
        }
    }
    
    private void recordMetrics(String serviceName, long duration, boolean success) {
        try {
            if (success) {
                metricsService.recordRequest(serviceName, duration, 0); // Bytes unknown
            } else {
                metricsService.recordError(serviceName);
            }
        } catch (Exception e) {
            log.warn("Failed to record metrics for service: {}", serviceName, e);
        }
    }
    
    private String extractServiceName(String requestUri) {
        try {
            String[] pathParts = requestUri.split("/");
            if (pathParts.length >= 4 && "api".equals(pathParts[1]) && "execution".equals(pathParts[2])) {
                return pathParts[3];
            }
        } catch (Exception e) {
            log.debug("Failed to extract service name from URI: {}", requestUri);
        }
        return "unknown";
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void clearMDC() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(SERVICE_NAME_KEY);
        MDC.remove(METHOD_KEY);
        MDC.remove(PATH_KEY);
    }
}
