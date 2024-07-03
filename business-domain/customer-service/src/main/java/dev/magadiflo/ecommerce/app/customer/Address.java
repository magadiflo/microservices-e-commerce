package dev.magadiflo.ecommerce.app.customer;

import lombok.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address {
    private String street;
    private String houseNumber;
    private String zipCode;
}
