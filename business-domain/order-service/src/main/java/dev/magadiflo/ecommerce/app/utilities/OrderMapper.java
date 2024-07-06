package dev.magadiflo.ecommerce.app.utilities;

import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderResponse;
import dev.magadiflo.ecommerce.app.models.entities.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public Order toOrder(OrderRequest request) {
        return Order.builder()
                .reference(request.reference())
                .totalAmount(request.amount())
                .paymentMethod(request.paymentMethod())
                .customerId(request.customerId())
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .customerId(order.getCustomerId())
                .build();
    }
}
