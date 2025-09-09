package com.example.feigngateway.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class RequestLoggingAspect {

    @Pointcut("execution(* com.example.feigngateway.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        
        if (request != null) {
            long startTime = System.currentTimeMillis();
            
            // Log incoming request
            log.info("=== INCOMING REQUEST ===");
            log.info("Method: {} {}", request.getMethod(), request.getRequestURI());
            log.info("Query String: {}", request.getQueryString());
            log.info("Remote Address: {}", request.getRemoteAddr());
            log.info("User-Agent: {}", request.getHeader("User-Agent"));
            log.info("Content-Type: {}", request.getContentType());
            log.info("Controller Method: {}.{}", 
                joinPoint.getTarget().getClass().getSimpleName(), 
                joinPoint.getSignature().getName());
            log.info("Arguments: {}", Arrays.toString(joinPoint.getArgs()));
            
            try {
                Object result = joinPoint.proceed();
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("=== RESPONSE ===");
                log.info("Status: SUCCESS");
                log.info("Duration: {}ms", duration);
                log.info("==================");
                
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("=== ERROR ===");
                log.error("Status: ERROR");
                log.error("Duration: {}ms", duration);
                log.error("Exception: {}", e.getMessage(), e);
                log.error("=============");
                throw e;
            }
        }
        
        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
