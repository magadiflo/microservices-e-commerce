package dev.magadiflo.ecommerce.app.services.impl;

import dev.magadiflo.ecommerce.app.clients.CustomerClient;
import dev.magadiflo.ecommerce.app.clients.ProductClient;
import dev.magadiflo.ecommerce.app.exceptions.BusinessException;
import dev.magadiflo.ecommerce.app.models.dtos.CustomerResponse;
import dev.magadiflo.ecommerce.app.models.dtos.OrderLineRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;
import dev.magadiflo.ecommerce.app.models.dtos.PurchaseResponse;
import dev.magadiflo.ecommerce.app.models.entities.Order;
import dev.magadiflo.ecommerce.app.repositories.OrderRepository;
import dev.magadiflo.ecommerce.app.services.OrderLineService;
import dev.magadiflo.ecommerce.app.services.OrderService;
import dev.magadiflo.ecommerce.app.utilities.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderLineService orderLineService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Long createdOrder(OrderRequest request) {
        CustomerResponse customerResponse = this.customerClient.findCustomer(request.customerId())
                .orElseThrow(() -> new BusinessException(String.format("No se puede crear la orden. El cliente con id %s no existe", request.customerId())));

        List<PurchaseResponse> purchaseResponses = this.productClient.purchaseProducts(request.products());

        Order orderDB = this.orderRepository.save(this.orderMapper.toOrder(request));

        request.products().forEach(pr -> {
            OrderLineRequest orderLineRequest = new OrderLineRequest(orderDB.getId(), pr.productId(), pr.quantity());
            this.orderLineService.saveOrderLine(orderLineRequest);
        });

        // TODO: start payment process

        // send the order confirmation --> notification-ms (kafka)
        return 0L;
    }

}
