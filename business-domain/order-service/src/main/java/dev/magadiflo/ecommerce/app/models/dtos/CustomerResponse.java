package dev.magadiflo.ecommerce.app.models.dtos;

public record CustomerResponse(String id,
                               String firstName,
                               String lastName,
                               String email,
                               Address address) {
}
