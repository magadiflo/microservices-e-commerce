package dev.magadiflo.notification.app.models.dtos;

import dev.magadiflo.notification.app.models.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentConfirmation(String orderReference,
                                  BigDecimal amount,
                                  PaymentMethod paymentMethod,
                                  String customerFirstName,
                                  String customerLastName,
                                  String customerEmail) {
}
