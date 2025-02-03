package com.springbit.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.dto.CryptoAction;
import com.springbit.crypto.model.dto.ReplyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name="spring-bit.services.kafka.enabled")
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

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        final Sinks.Many<Flux<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        final String[] currentSymbol = {null};

        Mono<Void> receive = session
                .receive() // receive a web socket message
                .map(webSocketMessage -> {
                    String receivedMessage = webSocketMessage.getPayloadAsText();
                    logger.info("Received websocket message: {}", receivedMessage);
                    try {
                        return objectMapper.readValue(receivedMessage, CryptoAction.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .doOnNext(cryptoAction -> {
                    if ("predict".equalsIgnoreCase(cryptoAction.operation()) && cryptoAction.symbol() != null) {
                        currentSymbol[0] = cryptoAction.symbol();
                        sink.tryEmitNext(kafkaService.consumeMessages(cryptoAction.symbol()));
                    }
                })
                .onErrorResume(error -> {
                    logger.warn("Error while handling websocket receive message: ", error);
                    return Mono.empty();
                })
                .concatMap(kafkaService::sendMessage)
                .then();

        Mono<Void> closed = session.closeStatus().then(kafkaService.sendMessage(
                new CryptoAction(currentSymbol[0], currentSymbol[0], "stop", 0f, LocalDateTime.now().toInstant(ZoneOffset.UTC), 0f)));

        Mono<Void> send = session.send(sink
                                        .asFlux()
                                        .switchMap(flux -> flux)
                                        .doOnNext(logger::info)
                                        .map(session::textMessage)
                                        .onErrorContinue((error, value) -> {
                                            logger.warn("Error while handling websocket publish message: ", error);
                                        })
                                ).onErrorContinue((error, value) -> {
                                    logger.warn("Error while handling websocket publish message: ", error);
                                }).then();

        return Mono.zip(receive, send, closed).then();
    }
}
