package ru.yandex.mymarketappskeleton.dto;

import lombok.Builder;

@Builder
public record Paging
        (
                int pageNumber,
                int pageSize,
                boolean hasNext,
                boolean hasPrevious
        ) {

}
