package dev.magadiflo.payment.app.controllers;

import dev.magadiflo.payment.app.models.dtos.PaymentRequest;
import dev.magadiflo.payment.app.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> createPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(this.paymentService.createPayment(request), HttpStatus.CREATED);
    }
}
