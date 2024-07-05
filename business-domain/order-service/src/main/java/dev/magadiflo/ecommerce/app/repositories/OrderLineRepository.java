package dev.magadiflo.ecommerce.app.repositories;

import dev.magadiflo.ecommerce.app.models.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}
