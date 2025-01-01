package com.springbit.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFlux
public class SpringBitGatewayApplication {
    static {
        System.setProperty("spring.cloud.bootstrap.enabled", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBitGatewayApplication.class, args);
    }

}
