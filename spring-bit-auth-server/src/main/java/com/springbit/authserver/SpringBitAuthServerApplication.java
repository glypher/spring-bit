package com.springbit.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBitAuthServerApplication {
    static {
        System.setProperty("spring.cloud.bootstrap.enabled", "true");

        // Set DNS cache TTL for successful lookups (positive results)
        System.setProperty("networkaddress.cache.ttl", "60");
        // Set DNS cache TTL for failed lookups (negative results)
        System.setProperty("networkaddress.cache.negative.ttl", "10");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBitAuthServerApplication.class, args);
    }

}
