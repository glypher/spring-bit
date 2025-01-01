package com.springbit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration
public class WebConfig {
    @Bean
    RouterFunction<?> routerFunction() {
        return RouterFunctions.resources("/**", new ClassPathResource("web/"))
                .and(RouterFunctions.resources("/", new ClassPathResource("web/index.html")));
    }

}
