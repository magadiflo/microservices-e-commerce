package dev.magadiflo.ecommerce.app.clients;

import dev.magadiflo.ecommerce.app.exceptions.BusinessException;
import dev.magadiflo.ecommerce.app.models.dtos.PurchaseRequest;
import dev.magadiflo.ecommerce.app.models.dtos.PurchaseResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductClient {

    @Value("${custom.config.product.url}")
    private String productUrl;

    @Value("${custom.config.product.path}")
    private String productPath;

    private RestClient restClient;
    private final RestClient.Builder restClientBuilder;

    @PostConstruct
    public void init() {
        this.restClient = this.restClientBuilder.baseUrl(this.productUrl + this.productPath).build();
    }

    public List<PurchaseResponse> purchaseProducts(List<PurchaseRequest> requestBody) {
        return this.restClient.post()
                .uri("/purchase")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new BusinessException("Error al procesar compra de productos:: " + response.getStatusText());
                })
                .body(new ParameterizedTypeReference<>() {
                });
    }

}
