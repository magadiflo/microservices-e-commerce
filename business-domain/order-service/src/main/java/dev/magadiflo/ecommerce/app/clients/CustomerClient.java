package dev.magadiflo.ecommerce.app.clients;

import dev.magadiflo.ecommerce.app.models.dtos.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "customer-service", url = "${custom.config.customer.url}", path = "${custom.config.customer.path}")
public interface CustomerClient {
    @GetMapping(path = "/{customerId}")
    Optional<CustomerResponse> findCustomer(@PathVariable String customerId);
}
