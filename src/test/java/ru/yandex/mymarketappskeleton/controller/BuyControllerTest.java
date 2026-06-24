package ru.yandex.mymarketappskeleton.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.mymarketappskeleton.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private WebSession session;

    @InjectMocks
    private BuyController buyController;

    @Test
    void buy_shouldReturnRedirectUrl() {

        when(session.getId()).thenReturn("session-1");
        when(orderService.createOrder("session-1"))
                .thenReturn(Mono.just(123L));

        StepVerifier.create(buyController.buy(session))
                .assertNext(viewName ->
                        assertEquals("redirect:/orders/123?newOrder=true", viewName)
                )
                .verifyComplete();

        verify(orderService).createOrder("session-1");
    }

    @Test
    void buy_shouldCallOrderServiceWithSessionId() {

        when(session.getId()).thenReturn("abc");
        when(orderService.createOrder("abc"))
                .thenReturn(Mono.just(1L));

        buyController.buy(session).block();

        verify(orderService).createOrder("abc");
    }
}
