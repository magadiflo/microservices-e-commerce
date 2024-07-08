package dev.magadiflo.payment.app.models.dtos;

import dev.magadiflo.payment.app.models.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentNotification(String orderReference,
                                  BigDecimal amount,
                                  PaymentMethod paymentMethod,
                                  String customerFirstName,
                                  String customerLastName,
                                  String customerEmail) {
}
