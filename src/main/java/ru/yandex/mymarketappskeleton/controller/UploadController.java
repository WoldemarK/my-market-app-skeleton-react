package ru.yandex.mymarketappskeleton.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.exception.ItemNotFoundException;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.service.ImageStorageService;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final ItemRepository itemRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    @PostMapping("/{id}/upload-image")
    public Mono<String> uploadImage(@PathVariable Long id,
                                    @RequestPart("file") FilePart file) {

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ItemNotFoundException("Item not found: " + id))))
                .flatMap(item -> imageStorageService.save(file)
                                .map(path -> {
                                    item.setImgPath(path);
                                    return item;
                                })
                )
                .flatMap(itemRepository::save)
                .thenReturn("redirect:/items/" + id);
    }
}