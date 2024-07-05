package dev.magadiflo.ecommerce.app.models.dtos;

import java.math.BigDecimal;

public record PurchaseResponse(Long productId,
                               String name,
                               String description,
                               BigDecimal price,
                               double quantity) {
}
