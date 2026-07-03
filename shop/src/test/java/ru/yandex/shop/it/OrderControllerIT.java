package ru.yandex.shop.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.OrderDto;
import ru.yandex.shop.service.OrderService;

import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "payment-service.url=http://localhost:8081"
        }
)
@AutoConfigureWebTestClient
public class OrderControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldReturnOrdersPage() {

        OrderDto o1 = OrderDto.builder()
                .id(1L)
                .build();


        OrderDto o2 = OrderDto.builder()
                .id(1L)
                .build();

        when(orderService.findAll()).thenReturn(Flux.just(o1, o2));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldReturnOrderById() {

        OrderDto order = OrderDto.builder()
                .id(1L)
                .build();

        when(orderService.findById(1L)).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturnOrderWithNewOrderFlag() {

        OrderDto order = OrderDto.builder()
                .id(1L)
                .build();

        when(orderService.findById(1L)).thenReturn(Mono.just(order));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }
}
