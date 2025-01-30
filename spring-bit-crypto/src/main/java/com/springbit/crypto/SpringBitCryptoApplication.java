package com.springbit.crypto;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableR2dbcRepositories
@EnableScheduling
public class SpringBitCryptoApplication {
    static {
        System.setProperty("spring.cloud.bootstrap.enabled", "true");

        // Set DNS cache TTL for successful lookups (positive results)
        System.setProperty("networkaddress.cache.ttl", "60");
        // Set DNS cache TTL for failed lookups (negative results)
        System.setProperty("networkaddress.cache.negative.ttl", "10");
    }

    public static void main(String[] args) {
        // Debug ssl handshake...
        //System.setProperty("javax.net.debug", "ssl:trustmanager");
        // To enable loading bootstrap.yml properties
        //System.setProperty("spring.cloud.bootstrap.enabled", "true");

        SpringApplication.run(SpringBitCryptoApplication.class, args);
    }

}
