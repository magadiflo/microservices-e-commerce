package dev.magadiflo.ecommerce.app.clients;

import dev.magadiflo.ecommerce.app.exceptions.BusinessException;
import dev.magadiflo.ecommerce.app.models.dtos.PaymentRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class PaymentClient {
    @Value("${custom.config.payment.url}")
    private String paymentUrl;

    @Value("${custom.config.payment.path}")
    private String paymentPath;

    private RestClient restClient;
    private final RestClient.Builder restClientBuilder;

    @PostConstruct
    public void init() {
        this.restClient = this.restClientBuilder.baseUrl(this.paymentUrl + this.paymentPath).build();
    }

    public Long requestOrderPayment(PaymentRequest paymentRequest) {
        return this.restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    InputStream responseBody = response.getBody();
                    String responseBodyAsString = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                    throw new BusinessException("Error al procesar pago de productos :: " + responseBodyAsString);
                })
                .body(Long.class);
    }
}
