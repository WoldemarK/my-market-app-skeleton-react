package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.mymarketappskeleton.enums.ActionType;
import ru.yandex.mymarketappskeleton.enums.SortType;

@Data
@Builder
public class CartUpdateRequest {
    private Long id;
    private ActionType action;
    private String search;
    private SortType sort;
    private int pageNumber;
    private int pageSize;
}
