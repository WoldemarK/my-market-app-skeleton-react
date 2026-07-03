package ru.yandex.paymentservice.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.shop.payment.dto.PaymentRequest;

import java.math.BigDecimal;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnBalance() {
        webTestClient.get()
                .uri("/api/payment/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance")
                .exists();
    }

    @Test
    void shouldMakePaymentSuccessfully() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(300));

        webTestClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Payment successful");
    }

    @Test
    void shouldFailPaymentWhenNotEnoughBalance() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(999999999));

        webTestClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange();
    }
}