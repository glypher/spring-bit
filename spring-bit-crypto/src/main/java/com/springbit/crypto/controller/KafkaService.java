package com.springbit.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.dto.CryptoAction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name="spring-bit.services.kafka.enabled")
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate;
    private final HashMap<String, Flux<String>> cryptoReceivers = new HashMap<>();
    private final ObjectMapper objectMapper;

    private @Value("${spring-bit.services.kafka.url}") String kafkaServer;
    private @Value("${spring-bit.services.kafka.action-topic}") String actionTopic;
    private @Value("${spring-bit.services.kafka.crypto-topic}") String cryptoTopic;

    public KafkaService(ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate,
                        ObjectMapper objectMapper) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> sendMessage(String topic, String message) {
        return kafkaProducerTemplate.send(topic, message)
                .doOnSuccess(result -> System.out.println("Message sent: " + message))
                .then();
    }

    public Mono<Void> sendMessage(CryptoAction cryptoAction) {
        // Send this to the kafka topic
        try {
            String message = objectMapper.writeValueAsString(cryptoAction);
            return kafkaProducerTemplate.send(actionTopic, message)
                    .doOnSuccess(result -> System.out.println("Message sent: " + message))
                    .then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate(
            String kafkaTopic) {
        Map<String, Object> config = Map.of(
                "bootstrap.servers", kafkaServer,
                "group.id", "springbit-" + kafkaTopic,
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "auto.offset.reset", "latest" // earliest for all
        );

        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(config);
        receiverOptions = receiverOptions.subscription(List.of(kafkaTopic));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }


    public Flux<String> consumeMessages(String symbol) {
        Flux<String> rcvFlux = cryptoReceivers.get(symbol);
        if (rcvFlux == null) {
            rcvFlux = reactiveKafkaConsumerTemplate(cryptoTopic + '-' + symbol)
                    .receive()
                    .map(ConsumerRecord::value)
                    .doOnNext(logger::debug)
                    .share();

            cryptoReceivers.put(symbol, rcvFlux);
        }
        return rcvFlux;
    }
}
