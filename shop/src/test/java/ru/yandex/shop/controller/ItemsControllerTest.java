package ru.yandex.shop.controller;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.dto.PageResponse;
import ru.yandex.shop.enums.ActionType;
import ru.yandex.shop.enums.SortType;
import ru.yandex.shop.service.CartService;
import ru.yandex.shop.service.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemsControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private WebSession session;

    @InjectMocks
    private ItemsController controller;

    @Test
    void getItems_shouldReturnPage() {

        when(session.getId()).thenReturn("s1");

        ItemDto item = ItemDto.builder()
                .id(1L)
                .title("Phone")
                .build();

        PageResponse<ItemDto> page = new PageResponse<>(List.of(item),
                1L,
                1,
                5,
                false,
                false);

        when(itemService.findItems(null, SortType.NO, 1, 5)).thenReturn(Mono.just(page));
        when(cartService.getCount("s1", 1L)).thenReturn(Mono.just(2));
        when(itemService.groupItems(any())).thenReturn(Mono.just(List.of(List.of(item))));

        Model model = new ConcurrentModel();

        StepVerifier.create(controller.getItems(
                        null,
                        SortType.NO,
                        1,
                        5,
                        session,
                        model))
                .expectNext("items")
                .verifyComplete();

        assertTrue(model.containsAttribute("items"));
        assertTrue(model.containsAttribute("paging"));

        verify(itemService).findItems(null, SortType.NO, 1, 5);
    }

    @Test
    void getItem_shouldReturnItemPage() {

        when(session.getId()).thenReturn("s1");

        ItemDto item = ItemDto.builder()
                .id(1L)
                .title("Phone")
                .build();

        when(itemService.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.getCount("s1", 1L)).thenReturn(Mono.just(3));

        Model model = new ConcurrentModel();

        StepVerifier.create(controller.getItem(1L, session, model))
                .expectNext("item")
                .verifyComplete();

        assertTrue(model.containsAttribute("item"));
    }

    @Test
    void updateItem_shouldCallPlus() {

        when(session.getId()).thenReturn("s1");
        when(cartService.plus("s1", 1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.updateItem(1L, ActionType.PLUS, session))
                .expectNext("redirect:/items/1")
                .verifyComplete();

        verify(cartService).plus("s1", 1L);
    }

    @Test
    void updateItem_shouldCallMinus() {

        when(session.getId()).thenReturn("s1");
        when(cartService.minus("s1", 1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.updateItem(1L, ActionType.MINUS, session))
                .expectNext("redirect:/items/1")
                .verifyComplete();

        verify(cartService).minus("s1", 1L);
    }
}