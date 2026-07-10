package ru.yandex.shop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.shop.exception.ItemNotFoundException;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.ImageStorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private UploadController uploadController;

    @Test
    void uploadImage_shouldSaveImageAndUpdateItem() {

        Item item = new Item();
        item.setId(1L);

        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(item));

        when(imageStorageService.save(filePart))
                .thenReturn(Mono.just("/images/test.jpg"));

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(uploadController.uploadImage(1L, filePart))
                .expectNext("redirect:/items/1")
                .verifyComplete();

        verify(imageStorageService).save(filePart);
        verify(itemRepository).save(any(Item.class));
    }


    @Test
    void uploadImage_shouldFailWhenItemNotFound() {

        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(uploadController.uploadImage(1L, filePart))
                .expectErrorMatches(error ->
                        error instanceof ItemNotFoundException && error.getMessage().contains("Item not found"))
                .verify();

        verify(imageStorageService, never()).save(any());
        verify(itemRepository, never()).save(any());
    }
}