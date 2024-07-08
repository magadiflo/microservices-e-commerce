package dev.magadiflo.ecommerce.app.services.impl;

import dev.magadiflo.ecommerce.app.clients.CustomerClient;
import dev.magadiflo.ecommerce.app.clients.PaymentClient;
import dev.magadiflo.ecommerce.app.clients.ProductClient;
import dev.magadiflo.ecommerce.app.exceptions.BusinessException;
import dev.magadiflo.ecommerce.app.kafka.OrderProducer;
import dev.magadiflo.ecommerce.app.models.dtos.*;
import dev.magadiflo.ecommerce.app.models.entities.Order;
import dev.magadiflo.ecommerce.app.repositories.OrderRepository;
import dev.magadiflo.ecommerce.app.services.OrderLineService;
import dev.magadiflo.ecommerce.app.services.OrderService;
import dev.magadiflo.ecommerce.app.utilities.OrderMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderLineService orderLineService;
    private final OrderMapper orderMapper;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAllOrders() {
        return this.orderRepository.findAll().stream()
                .map(this.orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long orderId) {
        return this.orderRepository.findById(orderId)
                .map(this.orderMapper::toOrderResponse)
                .orElseThrow(() -> new EntityNotFoundException("No existe la orden con el id " + orderId + " proporcionado"));
    }

    @Override
    @Transactional
    public Long createdOrder(OrderRequest request) {
        CustomerResponse customerResponse = this.customerClient.findCustomer(request.customerId())
                .orElseThrow(() -> new BusinessException(String.format("No se puede crear la orden. El cliente con id %s no existe", request.customerId())));

        List<PurchaseResponse> purchaseProducts = this.productClient.purchaseProducts(request.products());

        Order orderDB = this.orderRepository.save(this.orderMapper.toOrder(request));

        request.products().forEach(pr -> {
            OrderLineRequest orderLineRequest = new OrderLineRequest(orderDB.getId(), pr.productId(), pr.quantity());
            this.orderLineService.saveOrderLine(orderLineRequest);
        });

        PaymentRequest paymentRequest = new PaymentRequest(orderDB.getTotalAmount(), orderDB.getPaymentMethod(),
                orderDB.getId(), orderDB.getReference(), customerResponse);
        Long paymentId = this.paymentClient.requestOrderPayment(paymentRequest);
        log.info("Orden de pago exitoso, se generó el paymentId: {}", paymentId);

        OrderConfirmation orderConfirmation = new OrderConfirmation(request.reference(), request.amount(),
                request.paymentMethod(), customerResponse, purchaseProducts);
        this.orderProducer.sendOrderConfirmation(orderConfirmation);

        return orderDB.getId();
    }

}
