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
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.CartService;
import ru.yandex.shop.service.ItemService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemsControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private CartIdService cartIdService;

    @Mock
    private Authentication authentication;

    @Mock
    private WebSession session;

    @Mock
    private Model model;

    @InjectMocks
    private ItemsController controller;

    @Test
    void updateFromItems_ShouldCallPlus() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("id", "10");
        form.add("action", "PLUS");
        form.add("search", "phone");
        form.add("sort", "PRICE");
        form.add("pageNumber", "2");
        form.add("pageSize", "5");

        when(exchange.getFormData()).thenReturn(Mono.just(form));
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(cartIdService.getCartId(authentication, session)).thenReturn("cart");
        when(cartService.plus("cart", 10L)).thenReturn(Mono.empty());

        String result = controller.updateFromItems(exchange, authentication).block();

        assertEquals("redirect:/items?search=phone&sort=PRICE&pageNumber=2&pageSize=5", result);

        verify(cartService).plus("cart", 10L);
    }

    @Test
    void getItem_ShouldReturnItemPage() {

        when(cartIdService.getCartId(authentication, session)).thenReturn("cart-1");

        ItemDto item = ItemDto.builder()
                .id(10L)
                .title("Book")
                .description("Java")
                .imgPath("book.jpg")
                .price(BigDecimal.valueOf(50))
                .build();

        when(itemService.findById(10L)).thenReturn(Mono.just(item));
        when(cartService.getCount("cart-1", 10L)).thenReturn(Mono.just(3));

        String view = controller.getItem(10L, authentication, session, model).block();

        assertEquals("item", view);

        verify(model).addAttribute(eq("item"), any(ItemDto.class));
        verify(itemService).findById(10L);
        verify(cartService).getCount("cart-1", 10L);
    }
}