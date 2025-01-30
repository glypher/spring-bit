package com.springbit.gateway.configuration;

import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    RouterFunction<?> routerFunction(CacheConfig cacheConfig) {
        return RouterFunctions
                .route(
                        RequestPredicates.GET("/web-app")
                                .or(RequestPredicates.GET("/web-app/"))
                                .or(RequestPredicates.GET("/"))
                                .or(RequestPredicates.GET("/login"))
                                .or(RequestPredicates.GET("/loginCallback")),
                        request -> {
                            try {
                                String content = cacheConfig.loadHome();

                                return ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(content);
                            } catch (IOException e) {
                                return ServerResponse.status(500).bodyValue("Error reading the file: " + e.getMessage());
                            }
                        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/web-app/**")
                .addResourceLocations("classpath:web-app/")
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES));
    }

    static class PublicHost implements Condition {
        static String publicHost;

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var publicDomain = context.getEnvironment().getProperty("spring-bit.public-domain");
            var publicDomainUri = UriComponentsBuilder.fromUriString(publicDomain).build();
            publicHost = publicDomainUri.getHost();
            return (publicHost != null) && !publicHost.contains("localhost");
        }
    }

    @Bean
    @Conditional(PublicHost.class)
    public WebFilter wwwRedirectFilter() throws IOException {
        return  (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            var hostHeader = request.getHeaders().getHost();
            if (hostHeader != null) {
                String host = hostHeader.getHostName();

                if (!host.startsWith("www.") && host.contains(PublicHost.publicHost)) {
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
