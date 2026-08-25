package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StoreConfigDTO(
        String instagram,
        String whatsapp,
        @JsonProperty("freteGratis") BigDecimal freeShippingFrom,
        @JsonProperty("heroTitulo") String heroTitle,
        @JsonProperty("heroTexto") String heroText,
        @JsonProperty("confianca") List<Map<String, String>> trustBadges,
        @JsonProperty("processo") List<Map<String, String>> process,
        List<Map<String, String>> faq,
        @JsonProperty("rodape") String footer,
        @JsonProperty("obsPedido") String orderNotes) {
}
