package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CostParametersDTO(
        @JsonProperty("filPreco") BigDecimal filamentPricePerKg,
        @JsonProperty("potencia") BigDecimal powerKw,
        @JsonProperty("tarifa") BigDecimal energyRate,
        @JsonProperty("deprec") BigDecimal depreciationPerHour,
        @JsonProperty("mdo") BigDecimal laborPerHour,
        @JsonProperty("acresc") BigDecimal surchargePct,
        BigDecimal markup,
        @JsonProperty("comissao") BigDecimal marketplaceCommissionPct,
        @JsonProperty("taxaFixa") BigDecimal fixedFee) {
}
