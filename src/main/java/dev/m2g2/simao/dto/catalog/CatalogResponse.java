package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feed público consumido pela loja: produtos publicados + configuração da loja.
 *
 * O formato é o que o template da vitrine já lê, para que a loja estática e a
 * loja apontada para o catálogo vivo consumam exatamente a mesma coisa.
 */
public record CatalogResponse(
        @JsonProperty("marca") String brand,
        @JsonProperty("loja") StoreConfigDTO store,
        @JsonProperty("categorias") List<PublicCategory> categories,
        @JsonProperty("produtos") List<PublicProduct> products,
        @JsonProperty("publicadoEm") LocalDateTime publishedAt) {
}
