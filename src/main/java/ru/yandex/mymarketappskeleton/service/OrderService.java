package ru.yandex.mymarketappskeleton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Mono<Long> createOrder(String sessionId) {

        log.info("Create order started: sessionId={}", sessionId);

        return cartService.getRawCart(sessionId)
                .flatMap(cart -> {

                    log.debug("Cart loaded: {}", cart);

                    if (cart.isEmpty()) {
                        log.warn("Attempt to create order with empty cart: sessionId={}", sessionId);
                        return Mono.error(new EmptyCartException());
                    }

                    return itemRepository.findAllById(cart.keySet())
                            .collectList()
                            .flatMap(items -> {
                                if (items.isEmpty()) {
                                    return Mono.error(new EmptyCartException());
                                }
                                log.debug("Items loaded from DB: count={}", items.size());

                                Map<Long, Item> itemMap = items.stream()
                                        .collect(Collectors.toMap(Item::getId, i -> i));

                                Order order = new Order();

                                BigDecimal totalSum = BigDecimal.ZERO;

                                for (Map.Entry<Long, Integer> entry : cart.entrySet()) {

                                    Item item = itemMap.get(entry.getKey());

                                    if (item == null) {
                                        log.warn("Item not found in DB, skipping: itemId={}", entry.getKey());
                                        continue;
                                    }

                                    int count = entry.getValue();

                                    order.getItems().add(getOrderItem(order, item, count));

                                    BigDecimal itemSum = item.getPrice().multiply(BigDecimal.valueOf(count));

                                    totalSum = totalSum.add(itemSum);

                                    log.debug("Order item added: itemId={}, count={}, sum={}", item.getId(), count, itemSum);
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
                                                    .then(Mono.fromSupplier(() -> {
                                                        log.info("Cart cleared: sessionId={}",
                                                                sessionId);
                                                        return saved.getId();
                                                    }));
                                        });
                            });
                });
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
        return orderRepository.findAll()
                .map(orderMapper::toDto);
    }


    public Mono<OrderDto> findById(Long id) {
        log.debug("Find order by id={}", id);
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found: " + id)))
                .map(orderMapper::toDto);
    }
}
