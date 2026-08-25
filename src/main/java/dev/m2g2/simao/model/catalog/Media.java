package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Uma imagem guardada uma vez só, endereçada pelo hash do conteúdo — a
 * contraparte no servidor do mapa `midia` do frontend. As fotos a referenciam
 * como /api/media/{hash}.
 *
 * Os bytes ficam em disco, não no banco: guardá-los em bytea inflava o Postgres
 * (e o dump) sem ganho nenhum, e prendia a loja pública a uma consulta SQL para
 * servir cada imagem. Aqui fica só o metadado; quem sabe o caminho é o
 * MediaStorage.
 */
@Entity
public class Media extends BaseModel {

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
