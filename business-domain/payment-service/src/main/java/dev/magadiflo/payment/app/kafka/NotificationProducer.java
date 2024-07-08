package dev.magadiflo.payment.app.kafka;

import dev.magadiflo.payment.app.config.KafkaPaymentTopicConfig;
import dev.magadiflo.payment.app.models.dtos.PaymentNotification;
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
public class NotificationProducer {

    public final KafkaTemplate<String, PaymentNotification> kafkaTemplate;

    public void sendNotification(PaymentNotification paymentNotification) {
        log.info("Enviando notificación con la siguiente información: {}", paymentNotification);
        Message<PaymentNotification> message = MessageBuilder
                .withPayload(paymentNotification)
                .setHeader(KafkaHeaders.TOPIC, KafkaPaymentTopicConfig.PAYMENT_TOPIC)
                .build();
        this.kafkaTemplate.send(message);
    }

}
