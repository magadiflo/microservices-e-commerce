package dev.magadiflo.ecommerce.app.models.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderLineResponse {
    private Long id;
    double quantity;
}
