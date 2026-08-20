package dev.m2g2.simao.dto.catalog;

import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;

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
        String origem,
        String impressora,
        String filamento,
        CostBreakdown custo) {
}
