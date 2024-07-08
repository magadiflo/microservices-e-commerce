package dev.magadiflo.payment.app.services;

import dev.magadiflo.payment.app.models.dtos.PaymentRequest;

public interface PaymentService {
    Long createPayment(PaymentRequest paymentRequest);
}
