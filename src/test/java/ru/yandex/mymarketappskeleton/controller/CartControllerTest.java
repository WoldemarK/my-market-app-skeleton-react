package ru.yandex.mymarketappskeleton.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.mymarketappskeleton.enums.ActionType;
import ru.yandex.mymarketappskeleton.model.Item;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.service.CartService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartController cartController;


    @Test
    void getCart_shouldReturnCartPage() {

        when(session.getId()).thenReturn("s1");

        when(cartService.getRawCart("s1"))
                .thenReturn(Mono.just(Map.of(1L, 2)));

        Item item = new Item();
        item.setId(1L);
        item.setTitle("Phone");
        item.setDescription("desc");
        item.setImgPath("/img.png");
        item.setPrice(BigDecimal.valueOf(100));

        when(itemRepository.findAllById(Collections.singleton(any()))).thenReturn(Flux.just(item));

        Model model = new ConcurrentModel();

        StepVerifier.create(cartController.getCart(model, session))
                .expectNext("cart")
                .verifyComplete();

        assertTrue(model.containsAttribute("items"));
        assertTrue(model.containsAttribute("total"));

        verify(cartService).getRawCart("s1");
        verify(itemRepository).findAllById(Collections.singleton(any()));
    }

    @Test
    void updateCart_shouldCallPlus() {

        when(session.getId()).thenReturn("s1");
        when(cartService.plus("s1", 1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartController.updateCart(1L, ActionType.PLUS, session))
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        verify(cartService).plus("s1", 1L);
    }

    @Test
    void updateCart_shouldCallMinus() {

        when(session.getId()).thenReturn("s1");
        when(cartService.minus("s1", 1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartController.updateCart(1L, ActionType.MINUS, session))
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        verify(cartService).minus("s1", 1L);
    }

    @Test
    void updateCart_shouldCallDelete() {

        when(session.getId()).thenReturn("s1");
        when(cartService.delete("s1", 1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartController.updateCart(1L, ActionType.DELETE, session))
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        verify(cartService).delete("s1", 1L);
    }
}