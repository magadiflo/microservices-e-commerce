package dev.magadiflo.payment.app.services.impl;

import dev.magadiflo.payment.app.kafka.NotificationProducer;
import dev.magadiflo.payment.app.models.dtos.PaymentNotification;
import dev.magadiflo.payment.app.models.dtos.PaymentRequest;
import dev.magadiflo.payment.app.models.entities.Payment;
import dev.magadiflo.payment.app.repositories.PaymentRepository;
import dev.magadiflo.payment.app.services.PaymentService;
import dev.magadiflo.payment.app.utilities.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public Long createPayment(PaymentRequest paymentRequest) {
        Payment paymentDB = this.paymentRepository.save(this.paymentMapper.toPayment(paymentRequest));
        PaymentNotification paymentNotification =
                new PaymentNotification(paymentRequest.orderReference(),
                        paymentRequest.amount(), paymentRequest.paymentMethod(),
                        paymentRequest.customer().firstName(),
                        paymentRequest.customer().lastName(),
                        paymentRequest.customer().email());
        this.notificationProducer.sendNotification(paymentNotification);
        return paymentDB.getId();
    }
}
