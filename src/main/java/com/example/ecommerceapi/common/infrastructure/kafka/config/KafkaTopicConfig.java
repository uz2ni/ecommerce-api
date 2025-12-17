package com.example.ecommerceapi.common.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderPaidTopic(@Value("${kafka.topic.order-paid}") String topicName,
                                   @Value("${kafka.topic.common.partitions}") int partitions,
                                   @Value("${kafka.topic.common.replications}") short replicationFactors
                                   ) {
        return new NewTopic(topicName, partitions, replicationFactors);
    }

    @Bean
    public NewTopic testTopic() {
        return new NewTopic("testTopic", 2, (short)2);
    }

}