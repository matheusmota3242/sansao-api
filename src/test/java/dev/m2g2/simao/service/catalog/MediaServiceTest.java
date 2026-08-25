package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.MediaResponse;
import dev.m2g2.simao.model.catalog.Media;
import dev.m2g2.simao.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository repository;
    @Mock
    private MediaStorage storage;

    private MediaService service;

    private static final String PNG = "data:image/png;base64,"
            + Base64.getEncoder().encodeToString("fake-image-bytes".getBytes());

    @BeforeEach
    void setUp() {
        service = new MediaService(repository, storage);
        lenient().when(repository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void store_decodesAndReturnsHashUrl() {
        when(repository.findByHash(any())).thenReturn(Optional.empty());

        MediaResponse r = service.store(PNG);

        assertEquals(64, r.hash().length());            // sha-256 hex
        assertEquals("/api/media/" + r.hash(), r.url());
        assertEquals("image/png", r.contentType());
        assertEquals(16, r.bytes());
        verify(repository).save(any(Media.class));
    }

    @Test
    void store_sameImageTwice_reusesExistingRow() {
        Media existing = new Media();
        existing.setHash("abc");
        existing.setContentType("image/png");
        existing.setSizeBytes(16);
        when(repository.findByHash(any())).thenReturn(Optional.of(existing));
        when(storage.exists(any())).thenReturn(true);

        MediaResponse r = service.store(PNG);

        assertEquals("abc", r.hash());
        verify(repository, never()).save(any());
        verify(storage, never()).write(any(), any());
    }

    @Test
    void store_writesTheFileBeforeTheRow() {
        when(repository.findByHash(any())).thenReturn(Optional.empty());

        service.store(PNG);

        // A ordem importa: linha sem arquivo vira foto quebrada no catálogo.
        InOrder ordem = inOrder(storage, repository);
        ordem.verify(storage).write(any(), any());
        ordem.verify(repository).save(any(Media.class));
    }

    @Test
    void store_rewritesTheFileWhenOnlyTheRowSurvived() {
        Media existing = new Media();
        existing.setHash("abc");
        existing.setContentType("image/png");
        existing.setSizeBytes(16);
        when(repository.findByHash(any())).thenReturn(Optional.of(existing));
        when(storage.exists(any())).thenReturn(false);

        service.store(PNG);

        verify(storage).write(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void store_rejectsNonDataUri() {
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.store("https://example.com/foto.jpg"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void store_rejectsInvalidBase64() {
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.store("data:image/png;base64,!!!nao-e-base64!!!"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getByHash_missing_returns404() {
        when(repository.findByHash("nada")).thenReturn(Optional.empty());

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.getByHash("nada"));
        assertEquals(404, ex.getStatusCode().value());
    }
}
