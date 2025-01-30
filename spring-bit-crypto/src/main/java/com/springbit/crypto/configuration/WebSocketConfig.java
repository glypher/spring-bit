package com.springbit.crypto.configuration;

import com.springbit.crypto.controller.CryptoWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name="spring-bit.services.web-socket-crypto.enabled")
public class WebSocketConfig {
    @Bean
    public SimpleUrlHandlerMapping urlHandlerMapping(
            CryptoWebSocketHandler handler,
            @Value("${spring-bit.services.web-socket-crypto.url}") String wsUrl) {

        return new SimpleUrlHandlerMapping(Map.of(wsUrl, handler), 1);
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}