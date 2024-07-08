package dev.magadiflo.payment.app.utilities;

import dev.magadiflo.payment.app.models.dtos.PaymentRequest;
import dev.magadiflo.payment.app.models.entities.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toPayment(PaymentRequest request) {
        return Payment.builder()
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .orderId(request.orderId())
                .build();
    }
}
