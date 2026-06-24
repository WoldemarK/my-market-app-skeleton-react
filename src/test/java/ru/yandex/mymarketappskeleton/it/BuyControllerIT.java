package ru.yandex.mymarketappskeleton.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.controller.BuyController;
import ru.yandex.mymarketappskeleton.service.OrderService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(BuyController.class)
public class BuyControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void buy_shouldRedirectToOrderPage() {

        when(orderService.createOrder(anyString()))
                .thenReturn(Mono.just(123L));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals(
                        "Location",
                        "/orders/123?newOrder=true"
                );
    }
}

