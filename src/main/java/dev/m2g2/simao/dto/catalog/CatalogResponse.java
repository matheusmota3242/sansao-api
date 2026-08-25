package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Public catalog for the storefront: published products + store config.
 * Replaces the catalogo.json the app used to publish to object storage.
 */
public record CatalogResponse(
        @JsonProperty("loja") StoreConfigDTO store,
        @JsonProperty("cats") Map<String, String> categories,
        @JsonProperty("produtos") List<ProductResponse> products,
        @JsonProperty("publicadoEm") LocalDateTime publishedAt) {
}
