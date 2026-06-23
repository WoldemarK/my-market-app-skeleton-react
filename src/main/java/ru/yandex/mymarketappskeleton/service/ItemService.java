package ru.yandex.mymarketappskeleton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.dto.PageResponse;
import ru.yandex.mymarketappskeleton.enums.SortType;
import ru.yandex.mymarketappskeleton.exception.ItemNotFoundException;
import ru.yandex.mymarketappskeleton.mapper.ItemMapper;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public Mono<PageResponse<ItemDto>> findItems(String search,
                                                 SortType sortType,
                                                 int pageNumber,
                                                 int pageSize) {

        SortType actualSortType = sortType != null ? sortType : SortType.ALPHA;

        long offset = (long) (pageNumber - 1) * pageSize;

        log.debug("findItems called: search={}, sort={}, page={}, size={}",
                search,
                actualSortType,
                pageNumber,
                pageSize
        );

        return validatePage(pageNumber, pageSize)
                .then(itemRepository.findItems(
                                        search,
                                        actualSortType.name(),
                                        pageSize,
                                        offset
                                )
                                .map(itemMapper::toDto)
                                .collectList()
                                .zipWith(itemRepository.countItems(search))
                                .map(tuple -> {

                                    List<ItemDto> items = tuple.getT1();
                                    long total = tuple.getT2();

                                    boolean hasPrevious = pageNumber > 1;
                                    boolean hasNext = total > (long) pageNumber * pageSize;

                                    return new PageResponse<>(
                                            items,
                                            total,
                                            pageNumber,
                                            pageSize,
                                            hasNext,
                                            hasPrevious
                                    );
                                })
                )
                .doOnSuccess(page ->
                        log.debug(
                                "Items loaded: totalElements={}, page={}, size={}",
                                page.totalElements(),
                                page.pageNumber(),
                                page.pageSize()
                        ));
    }

    private Mono<Void> validatePage(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            return Mono.error(new IllegalArgumentException("Page number must be greater than 0"));
        }

        if (pageSize < 1) {
            return Mono.error(new IllegalArgumentException("Page size must be greater than 0"));
        }

        return Mono.empty();
    }

    public Mono<ItemDto> findById(Long id) {
        log.debug("findById called: id={}", id);

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Item not found: id={}", id);
                    return Mono.error(new ItemNotFoundException("Item not found: %d".formatted(id)));
                }))
                .doOnNext(item -> log.debug("Item found: id={}, title={}", item.getId(), item.getTitle()))
                .map(itemMapper::toDto);
    }

    public Mono<List<List<ItemDto>>> groupItems(Flux<ItemDto> items) {
        return items
                .collectList()
                .map(list -> {
                    log.debug("groupItems called: itemsSize={}", list.size());

                    List<List<ItemDto>> result = new ArrayList<>();

                    for (int i = 0; i < list.size(); i += 3) {
                        List<ItemDto> row = new ArrayList<>(list.subList(i, Math.min(i + 3, list.size())));

                        while (row.size() < 3) {
                            row.add(createEmptyItem());
                        }

                        result.add(row);
                    }

                    log.debug("Items grouped into rows: rowCount={}", result.size());

                    return result;
                });
    }

    private ItemDto createEmptyItem() {
        return ItemDto.builder()
                .id(-1L)
                .build();
    }
}