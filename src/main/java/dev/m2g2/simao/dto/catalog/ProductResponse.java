package dev.m2g2.simao.dto.catalog;

import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String sku,
        String cat,
        String catNome,
        Integer num,
        String tam,
        String nome,
        String desc,
        ProductStatus status,
        String obs,
        BigDecimal gram,
        BigDecimal tempo,
        BigDecimal trab,
        BigDecimal ins,
        BigDecimal emb,
        BigDecimal catalogo,
        boolean tempoExato,
        String foto,
        List<String> fotos,
        String origem,
        String impressora,
        String filamento,
        // storefront / SEO
        String slug,
        Integer prazo,
        Integer ordem,
        String material,
        String dimPeca,
        BigDecimal embPeso,
        String embDim,
        boolean publicado,
        boolean destaque,
        String descLonga,
        String metaDesc,
        String licenca,
        CostBreakdown custo) {
}
