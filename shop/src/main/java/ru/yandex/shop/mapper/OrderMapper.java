package ru.yandex.shop.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.shop.dto.OrderDto;
import ru.yandex.shop.dto.OrderItemDto;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.model.OrderItem;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .totalSum(order.getTotalSum())
                .orderDate(order.getOrderDate())
                .items(order.getItems()
                        .stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList()))
                .build();

    }

    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .itemId(item.getItemId())
                .title(item.getTitle())
                .price(item.getPrice())
                .count(item.getCount())
                .build();
    }
}
