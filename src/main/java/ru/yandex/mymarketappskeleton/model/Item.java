package ru.yandex.mymarketappskeleton.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("items")
public class Item {

    @Id
    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private BigDecimal price;

}
