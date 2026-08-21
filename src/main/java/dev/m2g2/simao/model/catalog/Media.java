package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * An image stored once, content-addressed by hash — the server-side counterpart
 * of the frontend's `midia` map. Photos reference it as /api/media/{hash}.
 */
@Entity
public class Media extends BaseModel {

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    // NÃO anotar com @Lob: no PostgreSQL o Hibernate mapeia @Lob byte[] para
    // Large Object (OID) e faz bind de um bigint, quebrando com
    // "column bytes is of type bytea but expression is of type bigint".
    // byte[] puro já mapeia para bytea.
    @Column(nullable = false)
    private byte[] bytes;

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

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
