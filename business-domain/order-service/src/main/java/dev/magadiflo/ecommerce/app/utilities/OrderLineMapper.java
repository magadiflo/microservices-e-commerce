package dev.magadiflo.ecommerce.app.utilities;

import dev.magadiflo.ecommerce.app.models.dtos.OrderLineRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderLineResponse;
import dev.magadiflo.ecommerce.app.models.entities.Order;
import dev.magadiflo.ecommerce.app.models.entities.OrderLine;
import org.springframework.stereotype.Component;

@Component
public class OrderLineMapper {

    public OrderLine toOrderLine(OrderLineRequest orderLineRequest) {
        return OrderLine.builder()
                .productId(orderLineRequest.productId())
                .quantity(orderLineRequest.quantity())
                .order(Order.builder().id(orderLineRequest.orderId()).build())
                .build();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return OrderLineResponse.builder()
                .id(orderLine.getId())
                .quantity(orderLine.getQuantity())
                .build();
    }
}
