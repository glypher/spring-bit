package com.springbit.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.dto.CryptoAction;
import com.springbit.crypto.model.dto.ReplyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Component
public class CryptoWebSocketHandler implements WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CryptoWebSocketHandler.class);

    private final KafkaService kafkaService;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    CryptoWebSocketHandler(
            KafkaService kafkaService,
            WebClient.Builder webBuilder,
            @Value("${spring-bit.services.ml-service.url}") String mlUrl,
            ObjectMapper objectMapper) {

        this.kafkaService = kafkaService;

        this.webClient = webBuilder.baseUrl(mlUrl).build();

        this.objectMapper = objectMapper;
    }

    private Mono<Void> startMlService(String symbol, boolean start) {
        return webClient
                .post()
                .uri("/crypto/" + symbol + (start? "/predict" : "stop"))
                .retrieve()
                .bodyToMono(ReplyStatus.class)
                .doOnNext(replyStatus -> {
                    logger.info("ML service status: {}", replyStatus);
                }).onErrorContinue(
                        (error, value) -> logger.warn("ML service status: ", error))
                .then();
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session
                .receive()
                .map(webSocketMessage -> {
                    String receivedMessage = webSocketMessage.getPayloadAsText();
                    logger.info("Received websocket message: {}", receivedMessage);
                    try {
                        return objectMapper.readValue(receivedMessage, CryptoAction.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .switchMap(cryptoAction -> {
                    if ("predict".equalsIgnoreCase(cryptoAction.operation()) && cryptoAction.symbol() != null) {
                        // Let ML service know to start prediction
                        return startMlService(cryptoAction.symbol(), true).then(
                                // Also link the websocket producer to kafka consumer
                                session.send(
                                        kafkaService.consumeMessages()
                                                .doOnNext(logger::info)
                                                .map(session::textMessage)
                                                .onErrorContinue((error, value) -> {
                                                    logger.warn("Error while handling websocket publish message: ", error);
                                                })
                        )).flux();
                    }
                    // Send this to the kafka topic
                    try {
                        return kafkaService.sendMessage("test-topic", objectMapper.writeValueAsString(cryptoAction)).flux();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onErrorResume(error -> {
                    logger.warn("Error while handling websocket receive message: ", error);
                    return Mono.empty();  // Fallback, just an empty Mono
                })
                .then();
    }
}
