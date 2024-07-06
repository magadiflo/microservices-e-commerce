package dev.magadiflo.ecommerce.app.controllers;

import dev.magadiflo.ecommerce.app.models.dtos.OrderLineResponse;
import dev.magadiflo.ecommerce.app.services.OrderLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/order-lines")
public class OrderLineController {

    private final OrderLineService service;

    @GetMapping(path = "/order/{orderId}")
    public ResponseEntity<List<OrderLineResponse>> findOrderLinesByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.service.findAllOrderLinesByOrderId(orderId));
    }

}
