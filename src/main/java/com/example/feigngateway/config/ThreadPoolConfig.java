package com.example.feigngateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolConfig {

    @Bean("gatewayTaskExecutor")
    public Executor gatewayTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - minimum threads that will be kept alive
        executor.setCorePoolSize(20);
        
        // Maximum pool size - maximum threads that can be created
        executor.setMaxPoolSize(100);
        
        // Queue capacity - number of tasks that can be queued
        executor.setQueueCapacity(500);
        
        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("Gateway-Async-");
        
        // Keep alive time for idle threads (in seconds)
        executor.setKeepAliveSeconds(60);
        
        // Rejection policy when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Maximum time to wait for tasks to complete (in seconds)
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("Gateway task executor initialized with core pool size: {}, max pool size: {}, queue capacity: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    @Bean("loggingTaskExecutor")
    public Executor loggingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Smaller pool for logging tasks
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Gateway-Logging-");
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        
        log.info("Logging task executor initialized with core pool size: {}, max pool size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
