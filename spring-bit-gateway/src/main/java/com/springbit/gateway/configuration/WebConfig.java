package com.springbit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

@Configuration
public class WebConfig {
    @Bean
    RouterFunction<?> routerFunction() {
        return RouterFunctions.resources("/web-app/**", new ClassPathResource("web-app/"))
                .andRoute(
                        RequestPredicates.GET("/web-app").or(RequestPredicates.GET("/web-app/")),
                        request -> ServerResponse.temporaryRedirect(URI.create("/web-app/index.html")).build());
    }

}
