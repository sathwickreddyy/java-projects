package com.sathwick.ewallet.userservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfigurations {
    // User service is a producer service
    // 1. ZooKeeper running
    // 2. Kafka Server / bootstrap running
    // /opt/homebrew/opt/kafka/bin/kafka-server-start /opt/homebrew/etc/kafka/server.properties
    // 3. Created a topic for user
    // Note: above 3 steps are outside the scope of this application.
    // This application needs
    // 1. Configure a producer
    // 2. Configure a template via which my code can communicate to kafka
    // 3. Send a kafka Event.

    public Map<String, Object> getKafkaProducerConfig(){
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configs;
    }

    @Bean
    public ProducerFactory<String, String> kafkaProducerFactory(){
        return new DefaultKafkaProducerFactory<String, String>(getKafkaProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(){
        return new KafkaTemplate<String, String>(kafkaProducerFactory());
    }
}
