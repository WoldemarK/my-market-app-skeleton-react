package ru.yandex.paymentservice.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.config.SecurityConfig;
import ru.yandex.paymentservice.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldAllowBalanceWithReadScope() {

        when(paymentService.getBalanceResponse(any())).thenReturn(new BalanceResponse());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_payments.read"))
                        .jwt(jwt -> jwt.claim("azp", "client")))
                .get()
                .uri("/balance")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldReturnForbiddenForBalanceWithoutReadScope() {

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_payments.write"))
                        .jwt(jwt -> jwt.claim("azp", "client")))
                .get()
                .uri("/balance")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() {

        webTestClient.get()
                .uri("/balance")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldAllowPaymentWithWriteScope() {

        when(paymentService.makePayment(any(), any())).thenReturn(new PaymentResponse());

        String body = """
                {
                  "recipientId":"123",
                  "amount":100
                }
                """;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_payments.write"))
                        .jwt(jwt -> jwt.claim("azp", "client")))
                .post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldReturnForbiddenForPaymentWithReadScope() {

        String body = """
                {
                  "recipientId":"123",
                  "amount":100
                }
                """;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_payments.read"))
                        .jwt(jwt -> jwt.claim("azp", "client")))
                .post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}