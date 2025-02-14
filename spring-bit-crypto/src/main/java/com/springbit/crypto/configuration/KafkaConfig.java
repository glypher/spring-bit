package com.springbit.crypto.configuration;

import com.springbit.crypto.services.CryptoType;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.*;

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

    @Bean
    public AdminClient reactiveAdminClient(@Value("${spring-bit.services.kafka.url}") String kafkaServer,
                                           @Value("${spring-bit.services.kafka.crypto-topic}") String cryptoTopic) {
        var adminClient = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer
        ));

        List<NewTopic> topics = new ArrayList<>();
        Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();

        var retentionConfig = new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, "600000"); // 10 minutes
        var cleanupConfig = new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
        var configOps = List.of(
                new AlterConfigOp(retentionConfig, AlterConfigOp.OpType.SET),
                new AlterConfigOp(cleanupConfig, AlterConfigOp.OpType.SET)
        );

        for (CryptoType cryptoType : CryptoType.values()) {
            if (cryptoType == CryptoType.UNKNOWN)
                continue;

            String topicName = cryptoTopic + "-" + cryptoType.getSymbol();

            topics.add(new NewTopic(topicName, 1, (short) 1));

            var topicResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

            configs.putIfAbsent(topicResource, configOps);
        }
        // Create the topics if it doesn't exist
        adminClient.createTopics(topics);

        // Configure topics retention settings
        adminClient.incrementalAlterConfigs(configs).all();

        return adminClient;
    }
}