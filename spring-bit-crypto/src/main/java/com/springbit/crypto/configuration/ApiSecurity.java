package com.springbit.crypto.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
@EnableWebFluxSecurity
public class ApiSecurity {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain apiHttpSecurity(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/**"))
                .authorizeExchange((exchanges) -> exchanges
                        .anyExchange().permitAll()
                );
        return http.build();
    }

}

