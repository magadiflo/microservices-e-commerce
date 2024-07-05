package dev.magadiflo.ecommerce.app.models.dtos;

public record OrderLineRequest(Long orderId, Long productId, Double quantity) {
}
