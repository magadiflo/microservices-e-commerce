package dev.magadiflo.ecommerce.app.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAllCustomers() {
        return ResponseEntity.ok(this.customerService.findAllCustomers());
    }

    @GetMapping(path = "/{customerId}")
    public ResponseEntity<CustomerResponse> findCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(this.customerService.findCustomer(customerId));
    }

    @GetMapping(path = "/exists/{customerId}")
    public ResponseEntity<Boolean> existsCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(this.customerService.existsCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return new ResponseEntity<>(this.customerService.createCustomer(request), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{customerId}")
    public ResponseEntity<String> updateCustomer(@Valid @RequestBody CustomerRequest request, @PathVariable String customerId) {
        return ResponseEntity.ok(this.customerService.updateCustomer(request, customerId));
    }

    @DeleteMapping(path = "/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        this.customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

}
