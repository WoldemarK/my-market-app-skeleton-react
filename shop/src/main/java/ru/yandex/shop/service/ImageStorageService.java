package ru.yandex.shop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    private final Path uploadDir = Paths.get("uploads");

    public ImageStorageService() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Upload directory initialized: {}", uploadDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Cannot create upload directory: {}", uploadDir.toAbsolutePath(), e);
            throw new RuntimeException("Cannot create upload dir", e);
        }
    }

    public Mono<String> save(FilePart file) {
        return Mono.defer(() -> {

            String original = file.filename();

            if (original == null || original.isBlank()) {
                log.warn("Attempt to upload empty file");
                return Mono.error(new IllegalArgumentException("Empty file"));
            }

            String ext = original.contains(".")
                    ? original.substring(original.lastIndexOf("."))
                    : ".jpg";

            String filename = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(filename);
            String resultPath = "/images/" + filename;

            log.info(
                    "Saving file: originalName={}, generatedName={}",
                    original,
                    filename
            );

            return file.transferTo(target)
                    .thenReturn(resultPath)
                    .doOnSuccess(path -> {
                        log.info("File saved successfully: {}", target.toAbsolutePath());
                        log.debug("Returning image path: {}", path);
                    })
                    .doOnError(error -> log.error("Failed to save file: {}", filename, error))
                    .onErrorMap(error -> new RuntimeException("Failed to save file", error)
                    );
        });
    }
}
