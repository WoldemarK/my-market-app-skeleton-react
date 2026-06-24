package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;
import ru.yandex.mymarketappskeleton.enums.ActionType;
import ru.yandex.mymarketappskeleton.enums.SortType;

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
