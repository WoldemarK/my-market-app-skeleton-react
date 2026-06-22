package ru.yandex.mymarketappskeleton.dto;

public record Paging(
        int pageNumber,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
