package ru.yandex.shop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.CartService;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartIdService cartIdService;

    @Mock
    private Model model;

    @Mock
    private WebSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartController cartController;

    @Test
    void getCart_ShouldReturnCartPage() {
        Map<Long, Integer> cart = Map.of(
                1L, 2,
                2L, 1
        );

        Item item1 = Item.builder()
                .id(1L)
                .title("Phone")
                .description("desc")
                .imgPath("img")
                .price(BigDecimal.valueOf(100))
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .title("Book")
                .description("desc")
                .imgPath("img")
                .price(BigDecimal.valueOf(50))
                .build();

        when(cartIdService.getCartId(authentication, session)).thenReturn("cart-1");
        when(cartService.getRawCart("cart-1")).thenReturn(Mono.just(cart));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item1, item2));

        String view = cartController.getCart(model, session, authentication).block();

        assertEquals("cart", view);

        verify(model).addAttribute(eq("items"), any());
        verify(model).addAttribute("total", BigDecimal.valueOf(250));
    }
    @Test
    void updateCart_ShouldCallPlus() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id", "5");
        form.add("action", "PLUS");

        when(exchange.getFormData()).thenReturn(Mono.just(form));
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(cartIdService.getCartId(authentication, session)).thenReturn("cart-1");
        when(cartService.plus("cart-1", 5L)).thenReturn(Mono.empty());

        String result = cartController.updateCart(exchange, authentication).block();

        assertEquals("redirect:/cart/items", result);

        verify(cartService).plus("cart-1", 5L);
    }
}