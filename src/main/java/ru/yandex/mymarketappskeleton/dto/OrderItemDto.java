package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record OrderItemDto
        (
                Long id,
                Long itemId,
                String title,
                BigDecimal price,
                Integer count
        ) {

}
