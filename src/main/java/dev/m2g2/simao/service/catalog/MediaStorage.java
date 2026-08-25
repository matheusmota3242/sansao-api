package dev.m2g2.simao.service.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda os bytes das imagens em disco, endereçados pelo hash.
 *
 * O caminho é <raiz>/<2 primeiros do hash>/<hash>: sem o prefixo, um catálogo
 * grande viraria um diretório único com milhares de arquivos, que alguns
 * sistemas de arquivos percorrem mal.
 *
 * A escrita é atômica (arquivo temporário + move): uma queda no meio do upload
 * não deixa arquivo truncado ocupando um hash que diz ser de outro conteúdo.
 */
@Component
public class MediaStorage {

    private static final Logger log = LoggerFactory.getLogger(MediaStorage.class);

    private final Path root;

    public MediaStorage(@Value("${application.media-path:media}") String path) {
        this.root = Path.of(path).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    public boolean exists(String hash) {
        return Files.isRegularFile(fileFor(hash));
    }

    public void write(String hash, byte[] bytes) {
        Path target = fileFor(hash);
        try {
            Files.createDirectories(target.getParent());
            Path temp = Files.createTempFile(target.getParent(), hash, ".part");
            try {
                Files.write(temp, bytes);
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            log.error("Falha ao gravar a imagem {} em {}", hash, target, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não consegui gravar a imagem no servidor.");
        }
    }

    public byte[] read(String hash) {
        Path file = fileFor(hash);
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            // O metadado existe no banco mas o arquivo não: volume trocado,
            // restore parcial, ou alguém apagou à mão.
            log.warn("Imagem {} está no banco mas não em {}", hash, file);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada.");
        }
    }

    /**
     * O hash vem da URL, então nunca é usado como caminho sem validação: só
     * hexadecimal, senão um "../.." leria qualquer arquivo do servidor.
     */
    private Path fileFor(String hash) {
        if (hash == null || !hash.matches("[0-9a-f]{64}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hash inválido.");
        return root.resolve(hash.substring(0, 2)).resolve(hash);
    }
}
