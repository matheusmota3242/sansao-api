package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * One product as it appears in an argilalab.json export (frontend field names).
 * Photos may come as a single `foto` (v1) or a `fotos` list (v4); entries can be
 * external URLs, data: URIs, or keys into the export's `midia` map.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImportProduct(
        String cat,
        Integer num,
        String tam,
        String nome,
        String desc,
        ProductStatus status,
        BigDecimal gram,
        BigDecimal tempo,
        BigDecimal trab,
        BigDecimal ins,
        BigDecimal emb,
        BigDecimal catalogo,
        Boolean tempoExato,
        String foto,
        List<String> fotos,
        String origem,
        String obs,
        String impressora,
        String filamento,
        // v4 storefront / SEO
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
