package dev.magadiflo.ecommerce.app.utilities;

import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseResponse;
import dev.magadiflo.ecommerce.app.models.dtos.ProductRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductResponse;
import dev.magadiflo.ecommerce.app.models.entities.Category;
import dev.magadiflo.ecommerce.app.models.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .availableQuantity(request.availableQuantity())
                .price(request.price())
                .category(Category.builder()
                        .id(request.categoryId())
                        .build())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .availableQuantity(product.getAvailableQuantity())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categoryDescription(product.getCategory().getDescription())
                .build();
    }

    public ProductPurchaseResponse toProductPurchaseResponse(Product productDB, Double quantity) {
        return ProductPurchaseResponse.builder()
                .productId(productDB.getId())
                .name(productDB.getName())
                .description(productDB.getDescription())
                .price(productDB.getPrice())
                .quantity(quantity)
                .build();
    }
}
