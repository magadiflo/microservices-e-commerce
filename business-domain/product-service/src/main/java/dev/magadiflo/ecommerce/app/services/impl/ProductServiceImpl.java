package dev.magadiflo.ecommerce.app.services.impl;

import dev.magadiflo.ecommerce.app.exceptions.ProductPurchaseException;
import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductPurchaseResponse;
import dev.magadiflo.ecommerce.app.models.dtos.ProductRequest;
import dev.magadiflo.ecommerce.app.models.dtos.ProductResponse;
import dev.magadiflo.ecommerce.app.models.entities.Product;
import dev.magadiflo.ecommerce.app.repositories.ProductRepository;
import dev.magadiflo.ecommerce.app.services.ProductService;
import dev.magadiflo.ecommerce.app.utilities.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAllProducts() {
        return this.productRepository.findAll().stream()
                .map(this.productMapper::toProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findProduct(Long productId) {
        return this.productRepository.findById(productId)
                .map(this.productMapper::toProductResponse)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el producto con id " + productId));
    }

    @Override
    @Transactional
    public Long createProduct(ProductRequest request) {
        Product product = this.productMapper.toProduct(request);
        return this.productRepository.save(product).getId();
    }

    @Override
    @Transactional
    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        // Obtenemos todos los id de los productos que se van a comprar
        List<Long> productIdsRequest = request.stream().map(ProductPurchaseRequest::productId).toList();

        // Verificamos si tenemos disponible todos los productos que se van a comprar
        List<Product> productsDB = this.productRepository.findAllByIdInOrderById(productIdsRequest);
        if (productIdsRequest.size() != productsDB.size()) {
            throw new ProductPurchaseException("Uno o varios productos no existen en la base de datos");
        }

        List<ProductPurchaseRequest> requestProductList = request.stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::productId))
                .toList();

        List<ProductPurchaseResponse> purchaseProducts = new ArrayList<>();

        for (int i = 0; i < productsDB.size(); i++) {
            Product productDB = productsDB.get(i);
            ProductPurchaseRequest productRequest = requestProductList.get(i);

            if (productDB.getAvailableQuantity() < productRequest.quantity()) {
                throw new ProductPurchaseException("Stock insuficiente para el producto con id " + productDB.getId());
            }

            double newAvailableQuantity = productDB.getAvailableQuantity() - productRequest.quantity();
            productDB.setAvailableQuantity(newAvailableQuantity);
            this.productRepository.save(productDB);

            purchaseProducts.add(this.productMapper.toProductPurchaseResponse(productDB, productRequest.quantity()));
        }

        return purchaseProducts;
    }
}
