package ru.yandex.shop.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.shop.dto.OrderDto;
import ru.yandex.shop.dto.OrderItemDto;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order, List<OrderItem> items) {

        return OrderDto.builder()
                .id(order.getId())
                .totalSum(order.getTotalSum())
                .orderDate(order.getOrderDate())
                .items(
                        items.stream()
                                .map(this::toItemDto)
                                .toList()
                )
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
