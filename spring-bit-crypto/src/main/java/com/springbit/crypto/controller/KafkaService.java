package com.springbit.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.dto.CryptoAction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnProperty(name="spring-bit.services.kafka.enabled")
public class KafkaService {
    private final ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate;
    private final ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
    private final ObjectMapper objectMapper;

    public KafkaService(ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate,
                        ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate,
                        ObjectMapper objectMapper) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> sendMessage(String topic, String message) {
        return kafkaProducerTemplate.send(topic, message)
                .doOnSuccess(result -> System.out.println("Message sent: " + message))
                .then();
    }

    public Mono<Void> sendMessage(String topic, CryptoAction cryptoAction) {
        // Send this to the kafka topic
        try {
            String message = objectMapper.writeValueAsString(cryptoAction);
            return kafkaProducerTemplate.send(topic, message)
                    .doOnSuccess(result -> System.out.println("Message sent: " + message))
                    .then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public Flux<String> consumeMessages() {
        return kafkaConsumerTemplate
                .receive()
                .map(ConsumerRecord::value);
    }
}
