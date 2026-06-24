package ru.yandex.mymarketappskeleton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.OrderDto;
import ru.yandex.mymarketappskeleton.exception.EmptyCartException;
import ru.yandex.mymarketappskeleton.exception.OrderNotFoundException;
import ru.yandex.mymarketappskeleton.mapper.OrderMapper;
import ru.yandex.mymarketappskeleton.model.Item;
import ru.yandex.mymarketappskeleton.model.Order;
import ru.yandex.mymarketappskeleton.model.OrderItem;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.repository.OrderRepository;


import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final TransactionalOperator transactionalOperator;

    public Mono<Long> createOrder(String sessionId) {
        return Mono.defer(() -> {

                    log.info("Create order started: sessionId={}", sessionId);

                    return cartService.getRawCart(sessionId)
                            .flatMap(cart -> {

                                log.debug("Cart loaded: sessionId={}, items={}", sessionId, cart);

                                if (cart.isEmpty()) {
                                    log.warn("Attempt to create order with empty cart: sessionId={}",
                                            sessionId
                                    );

                                    return Mono.error(new EmptyCartException());
                                }

                                return itemRepository.findAllById(cart.keySet())
                                        .collectList()
                                        .flatMap(items -> {

                                            log.debug("Items loaded from DB: count={}", items.size());

                                            Map<Long, Item> itemMap = items.stream()
                                                    .collect(Collectors.toMap(Item::getId, i -> i));

                                            Order order = new Order();
                                            BigDecimal totalSum = BigDecimal.ZERO;

                                            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {

                                                Item item = itemMap.get(entry.getKey());

                                                if (item == null) {
                                                    log.warn("Item not found in DB, skipping: itemId={}",
                                                            entry.getKey()
                                                    );
                                                    continue;
                                                }

                                                int count = entry.getValue();

                                                OrderItem orderItem = getOrderItem(order, item, count);

                                                order.getItems().add(orderItem);

                                                BigDecimal itemSum = item.getPrice().multiply(BigDecimal.valueOf(count));

                                                totalSum = totalSum.add(itemSum);

                                                log.debug("Order item added: itemId={}, count={}, sum={}",
                                                        item.getId(),
                                                        count,
                                                        itemSum
                                                );
                                            }
                                            order.setTotalSum(totalSum);

                                            BigDecimal finalTotalSum = totalSum;

                                            return orderRepository.save(order)
                                                    .flatMap(saved -> {
                                                        log.info("Order created successfully: orderId={}, totalSum={}",
                                                                saved.getId(),
                                                                finalTotalSum
                                                        );

                                                        return cartService.clear(sessionId)
                                                                .thenReturn(saved.getId())
                                                                .doOnSuccess(id ->
                                                                        log.info("Cart cleared after order: sessionId={}",
                                                                                sessionId
                                                                        )
                                                                );
                                                    });
                                        });
                            });
                })
                .as(transactionalOperator::transactional);
    }

    private static OrderItem getOrderItem(Order order, Item item, int count) {
        return OrderItem.builder()
                .order(order)
                .itemId(item.getId())
                .title(item.getTitle())
                .price(item.getPrice())
                .count(count)
                .build();
    }


    public Flux<OrderDto> findAll() {
        return Flux.defer(() ->
                orderRepository.findAll()
                        .map(orderMapper::toDto)
        );
    }

    public Mono<OrderDto> findById(Long id) {
        return Mono.defer(() -> {

            log.debug("Find order by id={}", id);

            return orderRepository.findById(id)
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                log.warn("Order not found: id={}", id);
                                return Mono.error(new OrderNotFoundException("Order not found: " + id));
                            })
                    )
                    .map(orderMapper::toDto);
        });
    }
}
