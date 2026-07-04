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
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.shop.client.PaymentClient;
import ru.yandex.shop.exception.EmptyCartException;
import ru.yandex.shop.mapper.OrderMapper;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

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

    private final String sessionId = "sessionId";
    private Map<Long, Integer> cart;
    private Item item;
    private BalanceResponse balanceResponse;
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;



    @BeforeEach
    void setUp() {
        cart = Map.of(1L, 2);
        item = new Item();
        item.setId(1L);
        item.setTitle("Apple");
        item.setPrice(BigDecimal.TEN);

        balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(BigDecimal.valueOf(1000));

        paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess(true);
        paymentResponse.setMessage("Payment successful");

        lenient().when(cartService.getRawCart(anyString())).thenReturn(Mono.just(cart));
        lenient().when(itemRepository.findAllById(Collections.singleton(any()))).thenReturn(Flux.just(item));
        lenient().when(paymentClient.getBalance()).thenReturn(Mono.just(balanceResponse));
        lenient().when(paymentClient.pay(any(PaymentRequest.class))).thenReturn(Mono.just(paymentResponse));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return Mono.just(order);
        });
        lenient().when(cartService.clear(anyString())).thenReturn(Mono.empty());
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
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
    void shouldCreateOrderSuccessfully() {

        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(cart.keySet()))
                .thenReturn(Flux.just(item));

        when(paymentClient.getBalance())
                .thenReturn(Mono.just(balanceResponse));

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
    void shouldFailWhenPaymentClientThrowsError() {

        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(cart.keySet()))
                .thenReturn(Flux.just(item));

        when(paymentClient.getBalance())
                .thenReturn(Mono.just(balanceResponse));

        when(paymentClient.pay(any(PaymentRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Payment service unavailable")));


        StepVerifier.create(orderService.createOrder(sessionId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldFailWhenGetBalanceFails() {

        when(cartService.getRawCart(sessionId))
                .thenReturn(Mono.just(cart));

        when(itemRepository.findAllById(cart.keySet()))
                .thenReturn(Flux.just(item));

        when(paymentClient.getBalance())
                .thenReturn(Mono.error(new RuntimeException("Balance service unavailable")));

        StepVerifier.create(orderService.createOrder(sessionId))
                .expectError(RuntimeException.class)
                .verify();
    }
}