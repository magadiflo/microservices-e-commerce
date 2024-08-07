package dev.magadiflo.ecommerce.app.services.impl;

import dev.magadiflo.ecommerce.app.models.dtos.OrderLineRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderLineResponse;
import dev.magadiflo.ecommerce.app.models.entities.OrderLine;
import dev.magadiflo.ecommerce.app.repositories.OrderLineRepository;
import dev.magadiflo.ecommerce.app.services.OrderLineService;
import dev.magadiflo.ecommerce.app.utilities.OrderLineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper orderLineMapper;

    @Override
    public List<OrderLineResponse> findAllOrderLinesByOrderId(Long orderId) {
        return this.orderLineRepository.findAllByOrderId(orderId).stream()
                .map(this.orderLineMapper::toOrderLineResponse)
                .toList();
    }

    @Override
    public Long saveOrderLine(OrderLineRequest orderLineRequest) {
        OrderLine orderLine = this.orderLineMapper.toOrderLine(orderLineRequest);
        return this.orderLineRepository.save(orderLine).getId();
    }
}
