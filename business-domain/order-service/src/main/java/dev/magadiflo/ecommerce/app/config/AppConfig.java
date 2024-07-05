package dev.magadiflo.ecommerce.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
