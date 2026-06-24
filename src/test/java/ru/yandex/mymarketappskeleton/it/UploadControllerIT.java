package ru.yandex.mymarketappskeleton.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.model.Item;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.service.ImageStorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UploadControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private ImageStorageService imageStorageService;

    @Test
    void uploadImage_shouldUploadAndRedirect() {

        Item item = new Item();
        item.setId(1L);

        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(item));

        when(imageStorageService.save(any()))
                .thenReturn(Mono.just("/images/test.jpg"));

        when(itemRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new byte[]{1, 2, 3})
                .filename("test.jpg");

        webTestClient.post()
                .uri("/1/upload-image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/items/1");
    }

}