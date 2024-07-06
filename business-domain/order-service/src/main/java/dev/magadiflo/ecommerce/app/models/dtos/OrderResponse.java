package dev.magadiflo.ecommerce.app.models.dtos;

import dev.magadiflo.ecommerce.app.models.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String reference;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String customerId;
}
