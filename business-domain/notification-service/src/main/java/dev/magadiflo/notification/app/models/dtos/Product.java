package dev.magadiflo.notification.app.models.dtos;

import java.math.BigDecimal;

public record Product(Long productId,
                      String name,
                      String description,
                      BigDecimal price,
                      double quantity) {
}
