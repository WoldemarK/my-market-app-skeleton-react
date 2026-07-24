package ru.yandex.paymentservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;


@SpringBootTest
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuatorShouldBeAccessibleWithoutToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldAllowReadBalance() {
        webTestClient
                .mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_payments.read")))
                .get()
                .uri("/balance/test")
                .exchange()
                .expectStatus()
                .isOk();
    }
}