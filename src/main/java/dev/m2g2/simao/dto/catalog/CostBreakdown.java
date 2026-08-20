package dev.m2g2.simao.dto.catalog;

import java.math.BigDecimal;

/**
 * Computed cost/pricing for a product, mirroring the frontend calc().
 * catalogo/margem/margemPct are null when the product has no catalog price
 * ("sob consulta").
 */
public record CostBreakdown(
        BigDecimal filamento,
        BigDecimal energia,
        BigDecimal depreciacao,
        BigDecimal maoDeObra,
        BigDecimal insumos,
        BigDecimal embalagem,
        BigDecimal subtotal,
        BigDecimal custoFinal,
        BigDecimal precoSugerido,
        BigDecimal precoMarketplace,
        BigDecimal catalogo,
        BigDecimal margem,
        BigDecimal margemPct) {
}
