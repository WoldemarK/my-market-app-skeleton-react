package ru.yandex.mymarketappskeleton.dto;


import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int pageNumber,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
