package com.springbit.crypto.controller;

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

    public KafkaService(ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate,
                        ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
    }

    public Mono<Void> sendMessage(String topic, String message) {
        return kafkaProducerTemplate.send(topic, message)
                .doOnSuccess(result -> System.out.println("Message sent: " + message))
                .then();
    }

    public Flux<String> consumeMessages() {
        return kafkaConsumerTemplate
                .receive()
                .map(ConsumerRecord::value);
    }
}
