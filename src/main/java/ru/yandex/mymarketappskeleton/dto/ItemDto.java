package ru.yandex.mymarketappskeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private Double price;

    private Integer count;
}
