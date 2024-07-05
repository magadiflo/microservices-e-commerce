package dev.magadiflo.ecommerce.app.controllers;

import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;
import dev.magadiflo.ecommerce.app.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@Valid @RequestBody OrderRequest request) {
        Long orderId = this.orderService.createdOrder(request);
        URI uriOrder = URI.create("/api/v1/orders/" + orderId);
        return ResponseEntity.created(uriOrder).body(orderId);
    }

}
