package com.springbit.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.model.dto.CryptoAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Component
public class CryptoWebSocketHandler implements WebSocketHandler {
    private final Sinks.Many<Crypto> sink = Sinks.many().replay().latest();
    private final Flux<String> sinkFlux;

    private final Sinks.Many<String> rcvSink = Sinks.many().multicast().onBackpressureBuffer();

    @Autowired
    ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(CryptoWebSocketHandler.class);

    CryptoWebSocketHandler() {
        sinkFlux = sink.asFlux().map(crypto -> {
            try {
                return objectMapper.writeValueAsString(crypto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rcvSink.asFlux()
                .map(data -> {
                    try {
                        return objectMapper.readValue(data, CryptoAction.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onErrorContinue((error, value) -> {
                    logger.warn("Error while handling websocket message: ", error);
                }).subscribe(cryptoAction -> {
                    logger.info("Crypto action: {}", cryptoAction);
                });
    }

    /*
    @KafkaListener(topics = "data-topic", groupId = "data-group")
    public void receiveMessage(Integer value) {
        String message = "{\"value\": " + value + "}";
        sink.tryEmitNext(message);
    }
    */
    @Scheduled(fixedRate = 5000) // Runs every 5 seconds
    public void runTask() {
        sink.tryEmitNext(new Crypto("BTC", "BTC", LocalDateTime.now(), 20.0f));
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        session.receive().doOnNext(webSocketMessage -> {
            String receivedMessage = webSocketMessage.getPayloadAsText();
            logger.info("Received websocket message: {}", receivedMessage);

            rcvSink.tryEmitNext(receivedMessage);
        });

        return session.send(sinkFlux.map(session::textMessage).onErrorContinue((error, value) -> {
            logger.warn("Error while handling websocket message: ", error);
        })).then(
                session.receive().doOnNext(webSocketMessage -> {
                    String receivedMessage = webSocketMessage.getPayloadAsText();
                    logger.info("Received websocket message: {}", receivedMessage);

                    rcvSink.tryEmitNext(receivedMessage);
                }).then());
    }
}
