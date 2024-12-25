/*
package com.springbit.crypto.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Configuration
public class DocsConfig implements WebFluxConfigurer {
    public static class DocsPrefixFilter implements WebFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String originalPath = request.getURI().getPath();

            if (originalPath.startsWith("/api/v1/docs/v3")) {
                String newPath = originalPath.substring("/api/v1/docs".length());

                ServerHttpRequest newRequest = request.mutate().path(newPath).build();

                exchange = exchange.mutate().request(newRequest).build();
            }

            return chain.filter(exchange);
        }
    }

    @Bean
    public WebFilter stripPathPrefixFilter() {
        return new DocsPrefixFilter();
    }

}
*/