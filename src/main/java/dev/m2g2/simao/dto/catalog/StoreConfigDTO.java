package dev.m2g2.simao.dto.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StoreConfigDTO(
        String instagram,
        String whatsapp,
        BigDecimal freteGratis,
        String heroTitulo,
        String heroTexto,
        List<Map<String, String>> confianca,
        List<Map<String, String>> processo,
        List<Map<String, String>> faq,
        String rodape,
        String obsPedido) {
}
