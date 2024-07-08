package dev.magadiflo.payment.app.repositories;

import dev.magadiflo.payment.app.models.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
