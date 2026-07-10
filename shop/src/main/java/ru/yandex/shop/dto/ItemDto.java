package ru.yandex.shop.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ItemDto
        (
                Long id,
                String title,
                String description,
                String imgPath,
                BigDecimal price,
                Integer count
        ) {
}
