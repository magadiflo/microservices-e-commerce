package dev.magadiflo.ecommerce.app.customer;

import dev.magadiflo.ecommerce.app.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> findAllCustomers() {
        return this.customerRepository.findAll().stream()
                .map(this.customerMapper::toCustomerResponse)
                .toList();
    }

    public CustomerResponse findCustomer(String customerId) {
        return this.customerRepository.findById(customerId)
                .map(this.customerMapper::toCustomerResponse)
                .orElseThrow(() -> new CustomerNotFoundException("No se encontró el cliente con id " + customerId));
    }

    public Boolean existsCustomer(String customerId) {
        return this.customerRepository.findById(customerId).isPresent();
    }

    public String createCustomer(CustomerRequest request) {
        Customer customer = this.customerMapper.toCustomer(request);
        return this.customerRepository.save(customer).getId();
    }

    public String updateCustomer(CustomerRequest request, String customerId) {
        return this.customerRepository.findById(customerId)
                .map(customerDB -> {
                    customerDB.setFirstName(request.firstName());
                    customerDB.setLastName(request.lastName());
                    customerDB.setEmail(request.email());
                    if (request.address() != null) {

                        Address addressDB = customerDB.getAddress();
                        if (addressDB == null) {
                            addressDB = Address.builder().build();
                        }

                        Address addressRequest = request.address();
                        if (addressRequest.getStreet() != null) {
                            addressDB.setStreet(addressRequest.getStreet().trim());
                        }

                        if (addressRequest.getHouseNumber() != null) {
                            addressDB.setHouseNumber(addressRequest.getHouseNumber().trim());
                        }

                        if (addressRequest.getZipCode() != null) {
                            addressDB.setZipCode(addressRequest.getZipCode().trim());
                        }

                        customerDB.setAddress(addressDB);
                    }
                    return customerDB;
                })
                .map(this.customerRepository::save)
                .map(Customer::getId)
                .orElseThrow(() -> new CustomerNotFoundException("Error en la actualización. Cliente con id " + customerId + " no encontrado"));
    }

    public void deleteCustomer(String customerId) {
        if (this.customerRepository.findById(customerId).isEmpty()) {
            throw new CustomerNotFoundException("Error al eliminar. Cliente con id " + customerId + " no encontrado");
        }
        this.customerRepository.deleteById(customerId);
    }
}
