package ru.yandex.mymarketappskeleton.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.mymarketappskeleton.dto.OrderDto;
import ru.yandex.mymarketappskeleton.exception.EmptyCartException;
import ru.yandex.mymarketappskeleton.exception.OrderNotFoundException;
import ru.yandex.mymarketappskeleton.mapper.OrderMapper;
import ru.yandex.mymarketappskeleton.model.Item;
import ru.yandex.mymarketappskeleton.model.Order;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldCreateOrder() {

        String sessionId = "session";

        Item item = new Item();
        item.setId(1L);
        item.setTitle("Phone");
        item.setPrice(BigDecimal.valueOf(100));

        Order savedOrder = new Order();
        savedOrder.setId(10L);

        when(cartService.getRawCart(sessionId)).thenReturn(Mono.just(Map.of(1L, 2)));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(cartService.clear(sessionId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(sessionId))
                .expectNext(10L)
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
        verify(cartService).clear(sessionId);
    }

    @Test
    void createOrder_shouldFailWhenCartEmpty() {

        when(cartService.getRawCart("session")).thenReturn(Mono.just(Map.of()));

        StepVerifier.create(orderService.createOrder("session"))
                .expectError(EmptyCartException.class)
                .verify();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldFailWhenItemsNotFound() {

        when(cartService.getRawCart("session")).thenReturn(Mono.just(Map.of(1L, 1)));

        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrder("session"))
                .expectError(EmptyCartException.class)
                .verify();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnOrder() {

        Order order = new Order();
        order.setId(1L);

        OrderDto dto = new OrderDto();

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        StepVerifier.create(orderService.findById(1L))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findById_shouldThrowWhenNotFound() {

        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.findById(1L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void findAll_shouldReturnMappedOrders() {

        Order order1 = new Order();
        Order order2 = new Order();

        OrderDto dto1 = new OrderDto();
        OrderDto dto2 = new OrderDto();

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        StepVerifier.create(orderService.findAll())
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();
    }
}
