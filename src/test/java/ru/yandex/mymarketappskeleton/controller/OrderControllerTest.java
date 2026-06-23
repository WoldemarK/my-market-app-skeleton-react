package ru.yandex.mymarketappskeleton.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.mymarketappskeleton.dto.OrderDto;
import ru.yandex.mymarketappskeleton.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {


    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void getOrders_shouldReturnOrdersPage() {

        OrderDto order1 = new OrderDto();
        OrderDto order2 = new OrderDto();

        when(orderService.findAll())
                .thenReturn(Flux.just(order1, order2));

        Model model = new ConcurrentModel();

        StepVerifier.create(orderController.getOrders(model))
                .expectNext("orders")
                .verifyComplete();

        assertTrue(model.containsAttribute("orders"));

        verify(orderService).findAll();
    }

    @Test
    void getOrder_shouldReturnOrderPage() {

        OrderDto order = new OrderDto();

        when(orderService.findById(1L)).thenReturn(Mono.just(order));

        Model model = new ConcurrentModel();

        StepVerifier.create(orderController.getOrder(1L, true, model))
                .expectNext("order")
                .verifyComplete();

        assertTrue(model.containsAttribute("order"));
        assertTrue(model.containsAttribute("newOrder"));

        verify(orderService).findById(1L);
    }

    @Test
    void getOrder_shouldWorkWithDefaultNewOrderFalse() {

        OrderDto order = new OrderDto();

        when(orderService.findById(2L)).thenReturn(Mono.just(order));

        Model model = new ConcurrentModel();

        StepVerifier.create(orderController.getOrder(2L, false, model))
                .expectNext("order")
                .verifyComplete();

        assertTrue(model.containsAttribute("order"));
        assertTrue(model.containsAttribute("newOrder"));

        verify(orderService).findById(2L);
    }
}