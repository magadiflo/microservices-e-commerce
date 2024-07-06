package dev.magadiflo.ecommerce.app.services;

import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderResponse;

import java.util.List;

public interface OrderService {
    List<OrderResponse> findAllOrders();

    OrderResponse findOrder(Long orderId);

    Long createdOrder(OrderRequest request);
}
