package com.example.feigngateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy
public class FeignGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignGatewayApplication.class, args);
    }
}
