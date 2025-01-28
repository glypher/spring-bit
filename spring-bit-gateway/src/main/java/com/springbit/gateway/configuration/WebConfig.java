package com.springbit.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfig {
    @Value("${spring-bit.public-domain}")
    private String publicDomain;

    @Bean
    RouterFunction<?> routerFunction() {
        return RouterFunctions.resources("/web-app/**", new ClassPathResource("web-app/"))
                .andRoute(
                        RequestPredicates.GET("/web-app")
                                .or(RequestPredicates.GET("/web-app/"))
                                .or(RequestPredicates.GET("/"))
                                .or(RequestPredicates.GET("/login"))
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

    @Bean
    public WebFilter wwwRedirectFilter() {
        return  (ServerWebExchange exchange, WebFilterChain chain) -> {
            if (publicDomain.startsWith("http://localhost"))
                return chain.filter(exchange);

            ServerHttpRequest request = exchange.getRequest();
            var hostHeader = request.getHeaders().getHost();
            if (hostHeader != null) {
                String host = hostHeader.getHostName();

                if (!host.startsWith("www.")) {
                    String newUrl = request.getURI().toString().replaceFirst("://", "://www.");
                    exchange.getResponse().setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                    exchange.getResponse().getHeaders().setLocation(URI.create(newUrl));
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
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
