package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * O produto como a loja pública o vê: só o que a vitrine mostra.
 *
 * É deliberadamente estreito. O ProductResponse do admin carrega custo, margem,
 * gramatura, tempo de impressão, impressora, filamento e observações internas —
 * nada disso é da conta de quem visita a loja, e /api/catalog não exige login.
 *
 * As chaves são as que o template da vitrine já lê (`preco`, `descricao`,
 * `prazoDias`...), por isso ficam em português como o resto do catálogo.
 */
public record PublicProduct(
        String sku,
        String slug,
        @JsonProperty("nome") String name,
        @JsonProperty("categoria") String categoryName,
        @JsonProperty("categoriaId") String categoryCode,
        @JsonProperty("resumo") String summary,
        @JsonProperty("descricao") String description,
        @JsonProperty("metaDescricao") String metaDescription,
        @JsonProperty("preco") BigDecimal price,
        @JsonProperty("fotos") List<String> photos,
        @JsonProperty("material") String material,
        @JsonProperty("dimensoes") String dimensions,
        @JsonProperty("prazoDias") Integer leadTimeDays,
        @JsonProperty("pesoEmbalado") BigDecimal packageWeight,
        @JsonProperty("dimEmbalagem") String packageDimensions,
        @JsonProperty("destaque") boolean featured,
        @JsonProperty("ordem") int sortOrder) {
}
