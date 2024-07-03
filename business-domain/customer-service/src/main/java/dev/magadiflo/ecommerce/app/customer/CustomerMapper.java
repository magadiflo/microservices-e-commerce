package dev.magadiflo.ecommerce.app.customer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toCustomer(CustomerRequest request) {
        return Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public CustomerResponse toCustomerResponse(Customer customerDB) {
        return CustomerResponse.builder()
                .id(customerDB.getId())
                .firstName(customerDB.getFirstName())
                .lastName(customerDB.getLastName())
                .email(customerDB.getEmail())
                .address(customerDB.getAddress())
                .build();
    }
}
