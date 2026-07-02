package ru.yandex.shop.dto;

import lombok.Builder;
import ru.yandex.shop.enums.ActionType;
import ru.yandex.shop.enums.SortType;


@Builder
public record CartUpdateRequest
        (
                Long id,
                ActionType action,
                String search,
                SortType sort,
                int pageNumber,
                int pageSize
        ) {

}
