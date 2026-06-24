package ru.yandex.mymarketappskeleton.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.model.Item;

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


