package ru.yandex.shop.it.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary  // Добавьте @Primary
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }
}
