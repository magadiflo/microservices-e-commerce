package dev.magadiflo.ecommerce.app.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaOrderTopicConfig {

    public final static String ORDER_TOPIC = "order-topic";

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(ORDER_TOPIC).build();
    }
}
