package dev.magadiflo.ecommerce.app.repositories;

import dev.magadiflo.ecommerce.app.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
