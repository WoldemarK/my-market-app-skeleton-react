package ru.yandex.shop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class BuyControllerTest {
    @Mock
    private OrderService orderService;

    @Mock
    private CartIdService cartIdService;

    @Mock
    private WebSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BuyController buyController;

    @Test
    void buy_ShouldRedirectToCreatedOrder() {

        when(cartIdService.getCartId(authentication, session)).thenReturn("cart-123");
        when(orderService.createOrder("cart-123")).thenReturn(Mono.just(42L));

        String result = buyController.buy(session, authentication).block();

        assertEquals("redirect:/orders/42?newOrder=true", result);

        verify(cartIdService).getCartId(authentication, session);
        verify(orderService).createOrder("cart-123");
    }
}
