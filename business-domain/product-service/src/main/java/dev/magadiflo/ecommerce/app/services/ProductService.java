package dev.magadiflo.ecommerce.app.services;

import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseResponse;
import dev.magadiflo.ecommerce.app.models.dtos.ProductRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findAllProducts();

    ProductResponse findProduct(Long productId);

    Long createProduct(ProductRequest request);

    List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request);
}
