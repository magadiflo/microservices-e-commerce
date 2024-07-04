package dev.magadiflo.ecommerce.app.repositories;

import dev.magadiflo.ecommerce.app.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdInOrderById(List<Long> productIds);
}
