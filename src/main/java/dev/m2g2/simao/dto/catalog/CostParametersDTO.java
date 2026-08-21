package dev.m2g2.simao.dto.catalog;

import java.math.BigDecimal;

public record CostParametersDTO(
        BigDecimal filPreco,
        BigDecimal potencia,
        BigDecimal tarifa,
        BigDecimal deprec,
        BigDecimal mdo,
        BigDecimal acresc,
        BigDecimal markup,
        BigDecimal comissao,
        BigDecimal taxaFixa) {
}
