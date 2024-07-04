package dev.magadiflo.ecommerce.app.models.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductPurchaseRequest(@NotNull(message = "El producto es obligatorio")
                                     Long productId,

                                     @Positive(message = "La cantidad debe ser positiva")
                                     @NotNull(message = "La cantidad es obligatorio")
                                     Double quantity) {
}
