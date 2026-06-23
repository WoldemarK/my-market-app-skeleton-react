package ru.yandex.mymarketappskeleton.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.OrderDto;
import ru.yandex.mymarketappskeleton.service.OrderService;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class OrderControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldReturnOrdersPage() {

        OrderDto o1 = new OrderDto();
        o1.setId(1L);

        OrderDto o2 = new OrderDto();
        o2.setId(2L);

        when(orderService.findAll()).thenReturn(Flux.just(o1, o2));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturnOrderById() {

        OrderDto order = new OrderDto();
        order.setId(1L);

        when(orderService.findById(1L)).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturnOrderWithNewOrderFlag() {

        OrderDto order = new OrderDto();
        order.setId(1L);

        when(orderService.findById(1L)).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }
}
