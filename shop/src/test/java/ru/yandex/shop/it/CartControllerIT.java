package ru.yandex.shop.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.CartService;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "payment-service.url=http://localhost:8081"
        }
)
@AutoConfigureWebTestClient
public class CartControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemRepository itemRepository;

    @Test
    void shouldReturnCartPage() {
        Map<Long, Integer> cart = Map.of(
                1L, 2,
                2L, 1
        );

        Item item1 = new Item(
                1L,
                "Apple",
                "desc1",
                "/img1",
                BigDecimal.valueOf(10)
        );

        Item item2 = new Item(
                2L,
                "Banana",
                "desc2",
                "/img2",
                BigDecimal.valueOf(20)
        );

        when(cartService.getRawCart(anyString()))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(anySet()))
                .thenReturn(Flux.just(item1, item2));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> {
                    String body = res.getResponseBody();
                    assert body != null;
                    assert body.contains("Apple");
                    assert body.contains("Banana");
                });
    }


}
