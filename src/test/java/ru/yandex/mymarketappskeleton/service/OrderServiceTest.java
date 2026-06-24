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
import ru.yandex.mymarketappskeleton.exception.OrderNotFoundException;
import ru.yandex.mymarketappskeleton.mapper.OrderMapper;
import ru.yandex.mymarketappskeleton.model.Order;
import ru.yandex.mymarketappskeleton.repository.OrderRepository;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;


    @Test
    void findById_shouldReturnOrder() {

        Order order = new Order();
        order.setId(1L);

        OrderDto dto = OrderDto.builder().build();

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

        OrderDto dto1 = OrderDto.builder().build();
        OrderDto dto2 = OrderDto.builder().build();

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        StepVerifier.create(orderService.findAll())
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();
    }
}
