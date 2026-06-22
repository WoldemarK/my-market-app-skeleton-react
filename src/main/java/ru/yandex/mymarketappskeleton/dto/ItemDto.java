package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ItemDto
        (
                Long id,
                String title,
                String description,
                String imgPath,
                BigDecimal price
        ) {
}
