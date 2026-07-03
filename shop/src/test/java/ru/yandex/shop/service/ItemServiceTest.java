package ru.yandex.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.exception.ItemNotFoundException;
import ru.yandex.shop.mapper.ItemMapper;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.repository.ItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setTitle("Phone");

        itemDto = ItemDto.builder()
                .id(1L)
                .title("Phone")
                .build();
    }


    @Test
    void findById_shouldReturnItem() {

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        StepVerifier.create(itemService.findById(1L))
                .expectNext(itemDto)
                .verifyComplete();
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {

        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.findById(1L))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    void groupItems_shouldGroupByThree() {

        ItemDto first = ItemDto.builder()
                .id(1L)
                .build();

        ItemDto second = ItemDto.builder()
                .id(2L)
                .build();

        ItemDto third = ItemDto.builder()
                .id(3L)
                .build();

        ItemDto fourth = ItemDto.builder()
                .id(4L)
                .build();

        Flux<ItemDto> flux = Flux.just(
                first,
                second,
                third,
                fourth
        );

        StepVerifier.create(itemService.groupItems(flux))
                .assertNext(groups -> {

                    assertEquals(2, groups.size());

                    assertEquals(3, groups.get(0).size());
                    assertEquals(3, groups.get(1).size());

                    assertEquals(1L, groups.get(0).get(0).id());
                    assertEquals(2L, groups.get(0).get(1).id());
                    assertEquals(3L, groups.get(0).get(2).id());

                    assertEquals(4L, groups.get(1).get(0).id());
                    assertEquals(-1L, groups.get(1).get(1).id());
                    assertEquals(-1L, groups.get(1).get(2).id());
                })
                .verifyComplete();
    }

    @Test
    void groupItems_shouldReturnEmptyList() {

        StepVerifier.create(itemService.groupItems(Flux.empty()))
                .assertNext(List::isEmpty)
                .verifyComplete();
    }
}