package dev.magadiflo.ecommerce.app.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(@NotBlank(message = "El primer nombre es requerido")
                              String firstName,

                              @NotBlank(message = "El apellido es requerido")
                              String lastName,

                              @NotBlank(message = "El correo es requerido")
                              @Email(message = "El correo no tiene un formato válido")
                              String email,

                              Address address) {
}
