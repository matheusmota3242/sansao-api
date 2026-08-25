package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.MediaResponse;
import dev.m2g2.simao.model.catalog.Media;
import dev.m2g2.simao.repository.MediaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Stores images content-addressed by hash, the server-side counterpart of the
 * frontend's `midia` map: uploading the same image twice returns the same hash
 * and stores the bytes once.
 */
@Service
public class MediaService {

    private static final int MAX_BYTES = 8 * 1024 * 1024;

    private final MediaRepository repository;
    private final MediaStorage storage;

    public MediaService(MediaRepository repository, MediaStorage storage) {
        this.repository = repository;
        this.storage = storage;
    }

    /** Accepts "data:image/jpeg;base64,...." and returns the stored reference. */
    public MediaResponse store(String dataUri) {
        if (dataUri == null || !dataUri.startsWith("data:")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envie uma data URI (data:image/...;base64,...).");
        }
        int comma = dataUri.indexOf(',');
        if (comma < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data URI malformada.");
        }
        String header = dataUri.substring(5, comma);
        if (!header.contains("base64")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só aceito data URI em base64.");
        }
        String contentType = header.substring(0, header.indexOf(';') < 0 ? header.length() : header.indexOf(';'));
        if (contentType.isBlank()) {
            contentType = "image/jpeg";
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUri.substring(comma + 1));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Base64 inválido.");
        }
        if (bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem vazia.");
        }
        if (bytes.length > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Imagem muito grande (máx. 8 MB). Comprima antes de enviar.");
        }

        String hash = sha256(bytes);
        Media media = repository.findByHash(hash).orElse(null);
        if (media == null) {
            // Grava o arquivo antes da linha: se a gravação falhar, não fica
            // metadado no banco apontando para um arquivo que não existe.
            storage.write(hash, bytes);
            media = new Media();
            media.setHash(hash);
            media.setContentType(contentType);
            media.setSizeBytes(bytes.length);
            LocalDateTime now = LocalDateTime.now();
            media.setCreatedAt(now);
            media.setUpdatedAt(now);
            media.setActive(true);
            media = repository.save(media);
        } else if (!storage.exists(hash)) {
            // Metadado sobreviveu ao arquivo (volume novo, restore parcial):
            // como o conteúdo é o mesmo hash, basta regravar.
            storage.write(hash, bytes);
        }
        return new MediaResponse(media.getHash(), urlFor(media.getHash()),
                media.getContentType(), (int) media.getSizeBytes());
    }

    /** Os bytes da imagem, lidos do disco. */
    public byte[] bytesOf(String hash) {
        getByHash(hash);   // 404 se nem o metadado existe
        return storage.read(hash);
    }

    public Media getByHash(String hash) {
        return repository.findByHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada."));
    }

    public static String urlFor(String hash) {
        return "/api/media/" + hash;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
