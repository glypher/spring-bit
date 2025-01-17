package com.springbit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;

@Configuration
public class WebConfig {
    @Bean
    RouterFunction<?> routerFunction() {
        return RouterFunctions.resources("/web-app/**", new ClassPathResource("web-app/"))
                .andRoute(
                        RequestPredicates.GET("/web-app")
                                .or(RequestPredicates.GET("/web-app/"))
                                .or(RequestPredicates.GET("/")),
                        request -> ServerResponse.temporaryRedirect(URI.create("/web-app/index.html")).build());
    }

    /*
    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120)).build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
                .build();
    }
    */
}
