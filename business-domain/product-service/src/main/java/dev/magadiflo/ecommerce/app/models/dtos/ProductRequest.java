package dev.magadiflo.ecommerce.app.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(@NotBlank(message = "El nombre del producto es requerido")
                             String name,

                             @NotBlank(message = "La descripción del producto es requerido")
                             String description,

                             @Positive(message = "La cantidad disponible debe ser positivo")
                             @NotNull(message = "La cantidad disponible no debe ser nulo")
                             Double availableQuantity,

                             @Positive(message = "El precio debe ser positivo")
                             @NotNull(message = "El precio no debe ser nulo")
                             BigDecimal price,

                             @NotNull(message = "La categoría del producto es requerido")
                             Long categoryId) {
}
