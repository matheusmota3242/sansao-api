package dev.m2g2.simao.dto.catalog;

import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product create/update payload. Field names mirror the ARGILA LAB frontend
 * (cat = category code, desc, tempo in hours, trab in minutes, ins/emb/catalogo).
 * `fotos` is the ordered photo list — external URLs or /api/media/{hash}; the
 * first one becomes the cover.
 */
public record ProductRequest(
        String nome,
        String cat,
        String tam,
        ProductStatus status,
        String obs,
        String desc,
        List<String> fotos,
        String origem,
        String impressora,
        String filamento,
        BigDecimal gram,
        BigDecimal tempo,
        BigDecimal trab,
        BigDecimal ins,
        BigDecimal emb,
        BigDecimal catalogo,
        // storefront / SEO
        String slug,
        Integer prazo,
        Integer ordem,
        String material,
        String dimPeca,
        BigDecimal embPeso,
        String embDim,
        Boolean publicado,
        Boolean destaque,
        String descLonga,
        String metaDesc,
        String licenca) {
}
