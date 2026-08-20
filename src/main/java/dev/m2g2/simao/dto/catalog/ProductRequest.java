package dev.m2g2.simao.dto.catalog;

import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;

/**
 * Product create/update payload. Field names mirror the ARGILA LAB frontend
 * (cat = category code, desc, tempo in hours, trab in minutes, ins/emb/catalogo).
 */
public record ProductRequest(
        String nome,
        String cat,
        String tam,
        ProductStatus status,
        String obs,
        String desc,
        String foto,
        String origem,
        String impressora,
        String filamento,
        BigDecimal gram,
        BigDecimal tempo,
        BigDecimal trab,
        BigDecimal ins,
        BigDecimal emb,
        BigDecimal catalogo) {
}
