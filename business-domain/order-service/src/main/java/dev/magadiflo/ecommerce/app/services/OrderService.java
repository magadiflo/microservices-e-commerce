package dev.magadiflo.ecommerce.app.services;

import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;

public interface OrderService {
    Long createdOrder(OrderRequest request);
}
