package ru.yandex.shop.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.service.CartService;
import ru.yandex.mymarketappskeleton.service.ItemService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ItemsControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void shouldLoadItemById() {

        ItemDto item =  ItemDto.builder()
                .id(1L)
                .title("Apple")
                .build();

        when(itemService.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.getCount(anyString(), eq(1L))).thenReturn(Mono.just(3));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldUpdateItemPlus() {

        when(cartService.plus(anyString(), eq(1L))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items/1?action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/1");
    }

    @Test
    void shouldUpdateItemMinus() {

        when(cartService.minus(anyString(), eq(1L))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items/1?action=MINUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/1");
    }
}
