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

    /**
     * DLQ 토픽: 처리 실패한 메시지를 저장
     */
    @Bean
    public NewTopic orderPaidDlqTopic(@Value("${kafka.topic.order-paid}") String topicName,
                                      @Value("${kafka.topic.common.partitions}") int partitions,
                                      @Value("${kafka.topic.common.replications}") short replicationFactors
    ) {
        return new NewTopic(topicName + ".DLQ", partitions, replicationFactors);
    }

}