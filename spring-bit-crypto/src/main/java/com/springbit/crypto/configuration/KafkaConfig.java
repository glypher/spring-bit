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
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
        );

        SenderOptions<String, String> senderOptions = SenderOptions.create(config);
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}