package dev.magadiflo.ecommerce.app.controllers;

import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseResponse;
import dev.magadiflo.ecommerce.app.models.dtos.ProductRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductResponse;
import dev.magadiflo.ecommerce.app.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAllProducts() {
        return ResponseEntity.ok(this.productService.findAllProducts());
    }

    @GetMapping(path = "/{productId}")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(this.productService.findProduct(productId));
    }

    @PostMapping
    public ResponseEntity<Long> createProduct(@Valid @RequestBody ProductRequest request) {
        Long productId = this.productService.createProduct(request);
        URI productUri = URI.create("/api/v1/products/" + productId);
        return ResponseEntity.created(productUri).body(productId);
    }

    @PostMapping(path = "/purchase")
    public ResponseEntity<List<ProductPurchaseResponse>> purchaseProducts(@RequestBody List<ProductPurchaseRequest> request) {
        return ResponseEntity.ok(this.productService.purchaseProducts(request));
    }

}
