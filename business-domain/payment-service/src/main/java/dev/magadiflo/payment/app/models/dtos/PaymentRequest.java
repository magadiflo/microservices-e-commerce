package dev.magadiflo.payment.app.models.dtos;

import dev.magadiflo.payment.app.models.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(@NotNull(message = "El monto es requerido")
                             @Positive(message = "El monto debe ser positivo")
                             BigDecimal amount,

                             @NotNull(message = "El método de pago es requerido")
                             PaymentMethod paymentMethod,

                             @NotNull(message = "La id de la orden es requerido")
                             Long orderId,

                             String orderReference,

                             @Valid
                             @NotNull(message = "Debe ingresar datos del cliente")
                             Customer customer) {
}
