package dev.magadiflo.notification.app.models.dtos;

import dev.magadiflo.notification.app.models.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(String orderReference,
                                BigDecimal totalAmount,
                                PaymentMethod paymentMethod,
                                Customer customer,
                                List<Product> products) {
}
