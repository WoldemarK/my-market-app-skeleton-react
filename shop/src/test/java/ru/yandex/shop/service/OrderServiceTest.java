package ru.yandex.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.shop.config.client.PaymentClient;
import ru.yandex.shop.exception.EmptyCartException;
import ru.yandex.shop.exception.PaymentFailedException;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private OrderService orderService;

    private final String sessionId = "session-1";
    @BeforeEach
    void setUp() {
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        Map<Long, Integer> cart = Map.of(1L, 2);

        Item item = new Item();
        item.setId(1L);
        item.setTitle("Apple");
        item.setPrice(BigDecimal.TEN);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess(true);

        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(cart.keySet()))
                .thenReturn(Flux.just(item));

        when(paymentClient.pay(any(PaymentRequest.class)))
                .thenReturn(Mono.just(paymentResponse));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return Mono.just(order);
                });

        when(cartService.clear(sessionId))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(sessionId))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCartEmpty() {
        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(Map.of()));
        StepVerifier.create(orderService.createOrder(sessionId))
                .expectError(EmptyCartException.class)
                .verify();
    }

    @Test
    void shouldFailWhenPaymentFails() {

        Map<Long, Integer> cart = Map.of(1L, 1);

        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.TEN);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess(false);

        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(cart.keySet()))
                .thenReturn(Flux.just(item));

        when(paymentClient.pay(any(PaymentRequest.class)))
                .thenReturn(Mono.just(paymentResponse));

        StepVerifier.create(orderService.createOrder(sessionId))
                .expectError(PaymentFailedException.class)
                .verify();
    }
}