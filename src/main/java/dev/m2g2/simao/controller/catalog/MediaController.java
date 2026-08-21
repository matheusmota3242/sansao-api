package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.MediaResponse;
import dev.m2g2.simao.dto.catalog.MediaUploadRequest;
import dev.m2g2.simao.model.catalog.Media;
import dev.m2g2.simao.service.catalog.MediaService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService service;

    public MediaController(MediaService service) {
        this.service = service;
    }

    @PostMapping
    public MediaResponse upload(@RequestBody MediaUploadRequest request) {
        return service.store(request.dataUri());
    }

    @GetMapping("/{hash}")
    public ResponseEntity<byte[]> get(@PathVariable String hash) {
        Media media = service.getByHash(hash);
        // Content-addressed: the bytes for a hash never change, so cache hard.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(media.getBytes());
    }
}
