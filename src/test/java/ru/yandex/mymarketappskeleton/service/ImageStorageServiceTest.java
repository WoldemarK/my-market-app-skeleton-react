package ru.yandex.mymarketappskeleton.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageStorageServiceTest {
    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        imageStorageService = new ImageStorageService();
    }

    @Test
    void save_shouldStoreFileAndReturnImagePath() {

        FilePart filePart = mock(FilePart.class);

        when(filePart.filename()).thenReturn("photo.jpg");
        when(filePart.transferTo(any(Path.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(imageStorageService.save(filePart))
                .assertNext(path -> {
                    assertTrue(path.startsWith("/images/"));
                    assertTrue(path.endsWith(".jpg"));
                })
                .verifyComplete();

        verify(filePart).filename();
        verify(filePart).transferTo(any(Path.class));
    }

    @Test
    void save_shouldUseDefaultJpgExtensionWhenFileHasNoExtension() {

        FilePart filePart = mock(FilePart.class);

        when(filePart.filename()).thenReturn("photo");
        when(filePart.transferTo(any(Path.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(imageStorageService.save(filePart))
                .assertNext(path -> {
                    assertTrue(path.startsWith("/images/"));
                    assertTrue(path.endsWith(".jpg"));
                })
                .verifyComplete();
    }

    @Test
    void save_shouldFailWhenFilenameIsNull() {

        FilePart filePart = mock(FilePart.class);

        when(filePart.filename()).thenReturn(null);

        StepVerifier.create(imageStorageService.save(filePart))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException
                                && "Empty file".equals(error.getMessage()))
                .verify();

        verify(filePart, never()).transferTo(any(Path.class));
    }

    @Test
    void save_shouldFailWhenFilenameIsBlank() {

        FilePart filePart = mock(FilePart.class);

        when(filePart.filename()).thenReturn(" ");

        StepVerifier.create(imageStorageService.save(filePart))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException
                                && "Empty file".equals(error.getMessage()))
                .verify();

        verify(filePart, never()).transferTo(any(Path.class));
    }

    @Test
    void save_shouldWrapTransferException() {

        FilePart filePart = mock(FilePart.class);

        when(filePart.filename()).thenReturn("photo.jpg");

        when(filePart.transferTo(any(Path.class)))
                .thenReturn(Mono.error(new IOException("Disk error")));

        StepVerifier.create(imageStorageService.save(filePart))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException
                                && error.getMessage().startsWith("Failed to save file:"))
                .verify();

        verify(filePart).transferTo(any(Path.class));
    }
}