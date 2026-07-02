package ru.yandex.shop.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.model.Item;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .imgPath(item.getImgPath())
                .price(item.getPrice())
                .build();

    }
}


