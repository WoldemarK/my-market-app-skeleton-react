package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDto {
    private Long id;
    private Long itemId;
    private String title;
    private BigDecimal price;
    private Integer count;
}
