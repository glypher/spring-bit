package com.springbit.gateway.configuration;

import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${spring-bit.auth.public-auth-server}")
    private String publicDomain;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {
        ServerRequestCache requestCache = new WebSessionServerRequestCache();

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
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


    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(
            WebClient.Builder webClientBuilder,
            @Value("${spring-bit.auth.gateway}") String gateway,
            @Value("${spring-bit.auth.server}") String server
    ) {
        WebClient webClient = webClientBuilder.baseUrl(server).build();
        return webClient.get()
                .uri("/.well-known/openid-configuration")
                .retrieve()
                .bodyToMono(JSONObject.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .map(jsonBody -> new InMemoryReactiveClientRegistrationRepository(
                        ClientRegistration
                                .withRegistrationId("springbit-openid")
                                .clientName("springbit-openid")
                                .clientId("springbit-openid")
                                .clientSecret("secret")
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .scope("openid", "profile", "monitoring.read")
                                .redirectUri(gateway + "/login/oauth2/code/springbit-openid")
                                .issuerUri(jsonBody.getAsString("issuer"))
                                .authorizationUri(replaceDomain(jsonBody.getAsString("authorization_endpoint")))
                                .tokenUri(jsonBody.getAsString("token_endpoint"))
                                .jwkSetUri(jsonBody.getAsString("jwks_uri"))
                                .userInfoUri(jsonBody.getAsString("userinfo_endpoint"))
                                .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
                                .userNameAttributeName("sub")
                                .build())
                ).block();
    }

    private String replaceDomain(String originalUrl) {
        try {
            // Create a URI object from the original URL
            URI uri = new URI(originalUrl);

            // Create a new URI with the same components but replacing the host (domain)
            URI modifiedUri = new URI(
                    uri.getScheme(),
                    publicDomain,
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );

            return modifiedUri.toString();
        } catch (URISyntaxException e) {
            return originalUrl;
        }
    }

}
