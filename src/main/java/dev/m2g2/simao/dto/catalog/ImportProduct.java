package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;

/**
 * One product as it appears in an argilalab.json export (frontend field names).
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
        String origem,
        String obs,
        String impressora,
        String filamento) {
}
