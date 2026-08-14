package com.demo.futuremovement.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configures the Kafka topic used for future movement messages.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${futuremovement.kafka.topic}")
    private String topicName;

    @Value("${futuremovement.kafka.partitions:3}")
    private int partitions;

    @Bean
    public NewTopic futureMovementsTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}
