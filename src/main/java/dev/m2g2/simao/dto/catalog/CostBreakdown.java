package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Computed cost/pricing for a product, mirroring the frontend calc().
 * catalogo/margem/margemPct are null when the product has no catalog price
 * ("sob consulta").
 */
public record CostBreakdown(
        @JsonProperty("filamento") BigDecimal filament,
        @JsonProperty("energia") BigDecimal energy,
        @JsonProperty("depreciacao") BigDecimal depreciation,
        @JsonProperty("maoDeObra") BigDecimal labor,
        @JsonProperty("insumos") BigDecimal supplies,
        @JsonProperty("embalagem") BigDecimal packaging,
        BigDecimal subtotal,
        @JsonProperty("custoFinal") BigDecimal finalCost,
        @JsonProperty("precoSugerido") BigDecimal suggestedPrice,
        @JsonProperty("precoMarketplace") BigDecimal marketplacePrice,
        @JsonProperty("catalogo") BigDecimal catalogPrice,
        @JsonProperty("margem") BigDecimal margin,
        @JsonProperty("margemPct") BigDecimal marginPct) {
}
