package ru.yandex.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.shop.payment.dto.PaymentRequest;
import ru.yandex.shop.config.client.PaymentClient;
import ru.yandex.shop.dto.OrderDto;
import ru.yandex.shop.exception.EmptyCartException;
import ru.yandex.shop.exception.OrderNotFoundException;
import ru.yandex.shop.exception.PaymentFailedException;
import ru.yandex.shop.mapper.OrderMapper;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.model.OrderItem;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<Long> createOrder(String sessionId) {
        return Mono.defer(() -> {
            log.info("Create order started: sessionId={}", sessionId);

            return cartService.getRawCart(sessionId)
                    .flatMap(cart -> {
                        if (cart.isEmpty()) {
                            return Mono.error(new EmptyCartException());
                        }

                        return itemRepository.findAllById(cart.keySet())
                                .collectList()
                                .flatMap(items -> {
                                    Map<Long, Item> itemMap = items.stream()
                                            .collect(Collectors.toMap(
                                                    Item::getId,
                                                    i -> i,
                                                    (a, b) -> a
                                            ));

                                    Order order = new Order();
                                    BigDecimal totalSum = BigDecimal.ZERO;

                                    for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                                        Item item = itemMap.get(entry.getKey());
                                        if (item == null) continue;

                                        int count = entry.getValue();
                                        OrderItem orderItem = OrderItem.builder()
                                                .order(order)
                                                .itemId(item.getId())
                                                .title(item.getTitle())
                                                .price(item.getPrice())
                                                .count(count)
                                                .build();

                                        order.getItems().add(orderItem);
                                        totalSum = totalSum.add(
                                                item.getPrice().multiply(BigDecimal.valueOf(count))
                                        );
                                    }

                                    order.setTotalSum(totalSum);

                                    PaymentRequest request = new PaymentRequest();
                                    request.setAmount(totalSum);

                                    return paymentClient.pay(request)
                                            .flatMap(payment -> {
                                                if (payment == null || !Boolean.TRUE.equals(payment.getSuccess())) {
                                                    return Mono.error(new PaymentFailedException("Payment failed"));
                                                }

                                                return transactionalOperator.transactional(
                                                        Mono.defer(() ->
                                                                orderRepository.save(order)
                                                                        .flatMap(saved ->
                                                                                cartService.clear(sessionId)
                                                                                        .thenReturn(saved.getId())
                                                                        )
                                                        )
                                                );
                                            });
                                });
                    });
        });
    }

    public Flux<OrderDto> findAll() {
        return orderRepository.findAll()
                .map(orderMapper::toDto);
    }

    public Mono<OrderDto> findById(Long id) {
        return Mono.defer(() -> {

            log.debug("Find order by id={}", id);

            return orderRepository.findById(id)
                    .switchIfEmpty(
                            Mono.defer(() ->
                            {
                                log.warn("Order not found: id={}", id);
                                return Mono.error(new OrderNotFoundException("Order not found: " + id));
                            })
                    )
                    .map(orderMapper::toDto);
        });
    }
}
