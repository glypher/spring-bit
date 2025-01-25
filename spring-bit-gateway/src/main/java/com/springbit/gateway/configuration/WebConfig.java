package com.springbit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class WebConfig {
    @Bean
    RouterFunction<?> routerFunction() {
        return RouterFunctions.resources("/web-app/**", new ClassPathResource("web-app/"))
                .andRoute(
                        RequestPredicates.GET("/web-app")
                                .or(RequestPredicates.GET("/web-app/"))
                                .or(RequestPredicates.GET("/"))
                                .or(RequestPredicates.GET("/loginCallback")),
                        request -> {
                            try {
                                // Load the file from the classpath
                                Resource resource = new ClassPathResource("web-app/index.html");

                                // Read the file content as a string
                                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

                                // Return the content as a response
                                return ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(content);
                            } catch (IOException e) {
                                return ServerResponse.status(500).bodyValue("Error reading the file: " + e.getMessage());
                            }
                            //return ServerResponse.temporaryRedirect(URI.create("/web-app/index.html")).build();
                        });
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
