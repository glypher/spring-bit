package com.springbit.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${spring-bit.public-domain}")
    private String publicDomain;

    @Bean
    @Order(1)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {

        JwtIssuerReactiveAuthenticationManagerResolver authenticationManagerResolver = JwtIssuerReactiveAuthenticationManagerResolver
                .fromTrustedIssuers("https://github.com/login/oauth");


        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable))
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(new ServerOAuth2AuthorizationRequestResolver() {
                            private final DefaultServerOAuth2AuthorizationRequestResolver defaultResolver = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);

                            @Override
                            public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
                                String path = exchange.getRequest().getPath().toString();
                                if (path.startsWith("/user")) {
                                    if (path.contains("keycloak")) {
                                        return resolve(exchange, "keycloak");
                                    } else if (path.contains("google")) {
                                        return resolve(exchange, "google");
                                    } else if (path.contains("github")) {
                                        return resolve(exchange, "github");
                                    } else if (path.contains("facebook")) {
                                        return resolve(exchange, "facebook");
                                    }
                                }
                                return Mono.empty();
                            }

                            @Override
                            public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
                                return this.defaultResolver.resolve(exchange, clientRegistrationId);
                            }
                        })
                        .authenticationSuccessHandler(
                                (webFilterExchange, authentication) -> {
                                        webFilterExchange.getExchange().getResponse()
                                                .setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                        webFilterExchange.getExchange().getResponse()
                                                .getHeaders().setLocation(
                                                        UriComponentsBuilder.fromUriString(publicDomain + "/web-app").queryParam("login", "ok").build().toUri());
                                        /*
                                        if (authentication.getPrincipal() instanceof OAuth2User) {
                                             OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                                                        clientRegistrationId,
                                                        oidcUser.getName());
                                            if (authorizedClient != null) {
                                                // Get the access token
                                                String accessToken = authorizedClient.getAccessToken().getTokenValue();
                                                System.out.println("Access Token: " + accessToken);
                                            }
                                        }
                                        */
                                        return Mono.empty();
                                })
                        .authenticationFailureHandler((WebFilterExchange webFilterExchange, AuthenticationException ex) -> {
                            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            webFilterExchange.getExchange().getResponse().getHeaders().setLocation(
                                    UriComponentsBuilder.fromUriString(publicDomain + "/web-app").queryParam("loginError", ex.getMessage()).build().toUri());
                            return Mono.empty();
                        }))
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(authenticationManagerResolver)
                )
                .authorizeExchange(authorize -> authorize
                        // Paths requiring OAuth2 login
                        .pathMatchers("/user/**").authenticated()

                        // Paths acting as a resource server (JWT validation)
                        .pathMatchers("/discovery/**","/tracing/**", "/grafana/**", "/prometheus/**").hasAnyAuthority("OAUTH2_USER", "OIDC_USER")
                        .anyExchange().permitAll()
                )
                .logout(logoutSpec -> logoutSpec.logoutUrl("/user/logout")
                        .logoutSuccessHandler((webFilterExchange, authentication) -> Mono.fromRunnable(() -> {
                            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
                            // Optionally, clear any custom session data here

                            webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(publicDomain));
                        }
                    )))
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.accessDeniedHandler((ServerWebExchange exchange, AccessDeniedException ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                            exchange.getResponse().getHeaders().setLocation(
                                    UriComponentsBuilder.fromUriString(publicDomain + "/web-app").queryParam("loginError", ex.getMessage()).build().toUri());
                            return Mono.empty();
                        }).authenticationEntryPoint((ServerWebExchange exchange, AuthenticationException ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                            exchange.getResponse().getHeaders().setLocation(
                                    UriComponentsBuilder.fromUriString(publicDomain + "/web-app").queryParam("loginError", ex.getMessage()).build().toUri());
                            return Mono.empty();
                        }))
                .build();
    }
}
