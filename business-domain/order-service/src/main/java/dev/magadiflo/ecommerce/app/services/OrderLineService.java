package dev.magadiflo.ecommerce.app.services;

import dev.magadiflo.ecommerce.app.models.dtos.OrderLineRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderLineResponse;

import java.util.List;

public interface OrderLineService {
    List<OrderLineResponse> findAllOrderLinesByOrderId(Long orderId);

    Long saveOrderLine(OrderLineRequest orderLineRequest);
}
