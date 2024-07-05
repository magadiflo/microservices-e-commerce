package dev.magadiflo.ecommerce.app.services;

import dev.magadiflo.ecommerce.app.models.dtos.OrderLineRequest;

public interface OrderLineService {
    Long saveOrderLine(OrderLineRequest orderLineRequest);
}
