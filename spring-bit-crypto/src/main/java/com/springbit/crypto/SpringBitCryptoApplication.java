package com.springbit.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpringBitCryptoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBitCryptoApplication.class, args);
    }

}
