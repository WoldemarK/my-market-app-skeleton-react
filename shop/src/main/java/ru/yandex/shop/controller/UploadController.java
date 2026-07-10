package ru.yandex.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Mono;
import ru.yandex.shop.exception.ItemNotFoundException;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.ImageStorageService;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final ItemRepository itemRepository;
    private final ImageStorageService imageStorageService;

    @PostMapping("/{id}/upload-image")
    public Mono<String> uploadImage(@PathVariable Long id,
                                    @RequestPart("file") FilePart file) {

        return Mono.defer(() ->
                itemRepository.findById(id)
                        .switchIfEmpty(
                                Mono.defer(() ->
                                        Mono.error(new ItemNotFoundException("Item not found: " + id)))
                        )
                        .flatMap(item -> imageStorageService.save(file)
                                        .map(path -> {
                                            item.setImgPath(path);
                                            return item;
                                        })
                        )
                        .flatMap(itemRepository::save)
                        .thenReturn("redirect:/items/" + id)
        );
    }
}