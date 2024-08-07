package dev.magadiflo.ecommerce.app.controllers;

import dev.magadiflo.ecommerce.app.models.dtos.OrderRequest;
import dev.magadiflo.ecommerce.app.models.dtos.OrderResponse;
import dev.magadiflo.ecommerce.app.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAllOrders() {
        return ResponseEntity.ok(this.orderService.findAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.orderService.findOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@Valid @RequestBody OrderRequest request) {
        Long orderId = this.orderService.createdOrder(request);
        URI uriOrder = URI.create("/api/v1/orders/" + orderId);
        return ResponseEntity.created(uriOrder).body(orderId);
    }

}
