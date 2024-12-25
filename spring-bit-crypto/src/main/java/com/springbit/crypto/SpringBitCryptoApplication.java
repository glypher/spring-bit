package com.springbit.crypto;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.reactive.config.EnableWebFlux;

@OpenAPIDefinition(
        servers = { @Server(url = "/api/v1", description = "Gateway prefix for the api") },
info = @Info(
        title = "Crypto API sample for chain data",
        version = "1.0.0",
        description = "Gets different chain information"))
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFlux
public class SpringBitCryptoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBitCryptoApplication.class, args);
    }

}
