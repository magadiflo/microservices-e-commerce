package dev.magadiflo.notification.app.models.documents;

import dev.magadiflo.notification.app.models.dtos.OrderConfirmation;
import dev.magadiflo.notification.app.models.dtos.PaymentConfirmation;
import dev.magadiflo.notification.app.models.enums.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private NotificationType type;
    private LocalDateTime notificationDate;
    private OrderConfirmation orderConfirmation;
    private PaymentConfirmation paymentConfirmation;
}
