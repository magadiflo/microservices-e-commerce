package dev.magadiflo.notification.app.kafka;

import dev.magadiflo.notification.app.models.documents.Notification;
import dev.magadiflo.notification.app.models.dtos.OrderConfirmation;
import dev.magadiflo.notification.app.models.dtos.PaymentConfirmation;
import dev.magadiflo.notification.app.models.dtos.Product;
import dev.magadiflo.notification.app.models.enums.NotificationType;
import dev.magadiflo.notification.app.repositories.NotificationRepository;
import dev.magadiflo.notification.app.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-topic", groupId = "payment-notification")
    public void consumePaymentConfirmationNotification(PaymentConfirmation paymentConfirmation) {
        log.info("Consumiendo mensaje desde el topic payment-topic: {}", paymentConfirmation);
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .paymentConfirmation(paymentConfirmation)
                .build();
        this.notificationRepository.save(notification);

        String destinationEmail = paymentConfirmation.customerEmail();
        String customerName = "%s %s".formatted(paymentConfirmation.customerFirstName(), paymentConfirmation.customerLastName());
        BigDecimal amount = paymentConfirmation.amount();
        String orderReference = paymentConfirmation.orderReference();

        this.emailService.sendPaymentSuccessEmail(destinationEmail, customerName, amount, orderReference);
    }

    @KafkaListener(topics = "order-topic", groupId = "order-notification")
    public void consumeOrderConfirmationNotification(OrderConfirmation orderConfirmation) {
        log.info("Consumiendo mensaje desde el topic order-topic: {}", orderConfirmation);
        Notification notification = Notification.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .orderConfirmation(orderConfirmation)
                .build();
        this.notificationRepository.save(notification);

        String destinationEmail = orderConfirmation.customer().email();
        String customerName = "%s %s".formatted(orderConfirmation.customer().firstName(), orderConfirmation.customer().lastName());
        BigDecimal totalAmount = orderConfirmation.totalAmount();
        String orderReference = orderConfirmation.orderReference();
        List<Product> products = orderConfirmation.products();

        this.emailService.sendOrderConfirmationEmail(destinationEmail, customerName, totalAmount, orderReference, products);
    }

}
