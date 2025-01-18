package com.springbit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import reactor.core.publisher.Mono;

import java.net.URI;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {
        ServerRequestCache requestCache = new WebSessionServerRequestCache();

        http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/discovery/**","/tracing/**", "/grafana/**", "/prometheus/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/oauth2/authorization/springbit-openid")
                        .authenticationSuccessHandler(
                                (webFilterExchange, authentication) -> requestCache
                                        .getRedirectUri(webFilterExchange.getExchange())
                                        .defaultIfEmpty(URI.create("/")) // Redirect here if no saved request
                                        .flatMap(redirectUrl -> {
                                            webFilterExchange.getExchange().getResponse()
                                                    .setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                            webFilterExchange.getExchange().getResponse()
                                                    .getHeaders().setLocation(redirectUrl);
                                            return Mono.empty();
                                        })))
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> {
                    OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler =
                            new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
                    logoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8080/");
                    logout.logoutSuccessHandler(logoutSuccessHandler);
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Needed to hash the oauth2 client secret {bcrypt}....
        return new BCryptPasswordEncoder();
    }

}
