package dev.magadiflo.ecommerce.app.models.dtos;

import dev.magadiflo.ecommerce.app.models.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount,
                             PaymentMethod paymentMethod,
                             Long orderId,
                             String orderReference,
                             CustomerResponse customer) {
}