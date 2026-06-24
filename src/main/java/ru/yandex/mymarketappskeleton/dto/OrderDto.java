package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDto
        (
                Long id,
                List<OrderItemDto> items,
                BigDecimal totalSum,
                LocalDateTime orderDate
        ) {

}



