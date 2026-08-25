package dev.m2g2.simao.dto.management;

import java.math.BigDecimal;

/**
 * Criação e edição de pedido. `customer` aceita id ou nome — um nome que não
 * existe vira cliente novo, que é o que permite registrar o pedido numa tela só.
 */
public record OrderRequest(
        String description,
        String customer,
        Integer printTimeMinutes,
        BigDecimal productionCost,
        BigDecimal salePrice,
        String observations) {
}
