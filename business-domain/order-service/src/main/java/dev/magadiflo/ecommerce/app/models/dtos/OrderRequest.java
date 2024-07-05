package dev.magadiflo.ecommerce.app.models.dtos;

import dev.magadiflo.ecommerce.app.models.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(String reference,

                           @NotNull(message = "El monto no debe ser nulo")
                           @Positive(message = "El monto debe ser positivo")
                           BigDecimal amount,

                           @NotNull(message = "El método de pago no debe ser nulo")
                           PaymentMethod paymentMethod,

                           @NotBlank(message = "El cliente debe estar presente")
                           String customerId,

                           @NotEmpty(message = "Deberías comprar al menos un producto")
                           List<@Valid PurchaseRequest> products) {
}
