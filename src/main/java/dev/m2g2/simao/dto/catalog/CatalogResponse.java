package dev.m2g2.simao.dto.catalog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Public catalog for the storefront: published products + store config.
 * Replaces the catalogo.json the app used to publish to object storage.
 */
public record CatalogResponse(
        StoreConfigDTO loja,
        Map<String, String> cats,
        List<ProductResponse> produtos,
        LocalDateTime publicadoEm) {
}
