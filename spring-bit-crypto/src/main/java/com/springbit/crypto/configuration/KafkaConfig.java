package com.springbit.crypto.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.receiver.ReceiverOptions;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name="spring-bit.services.kafka.enabled")
public class KafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate(
            @Value("${spring-bit.services.kafka.url}") String kafkaServer) {
        Map<String, Object> config = Map.of(
                "bootstrap.servers", kafkaServer,
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
        );

        SenderOptions<String, String> senderOptions = SenderOptions.create(config);
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }


    @Bean
    public ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate(
            @Value("${spring-bit.services.kafka.url}") String kafkaServer,
            @Value("${spring-bit.services.kafka.topic}") String kafkaTopic) {
        Map<String, Object> config = Map.of(
                "bootstrap.servers", kafkaServer,
                "group.id", "springbit",
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "auto.offset.reset", "latest" // earliest for all
        );

        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(config);
        receiverOptions = receiverOptions.subscription(List.of(kafkaTopic));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}