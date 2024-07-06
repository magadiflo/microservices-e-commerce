package dev.magadiflo.ecommerce.app.kafka;

import dev.magadiflo.ecommerce.app.config.KafkaOrderTopicConfig;
import dev.magadiflo.ecommerce.app.models.dtos.OrderConfirmation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderConfirmation> kafkaTemplate;

    public void sendOrderConfirmation(OrderConfirmation orderConfirmation) {
        log.info("Enviando confirmación de la orden");
        Message<OrderConfirmation> message = MessageBuilder
                .withPayload(orderConfirmation)
                .setHeader(KafkaHeaders.TOPIC, KafkaOrderTopicConfig.ORDER_TOPIC)
                .build();

        this.kafkaTemplate.send(message);
    }
}
