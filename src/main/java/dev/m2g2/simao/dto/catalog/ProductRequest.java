package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product create/update payload. Field names mirror the ARGILA LAB frontend
 * (cat = category code, desc, tempo in hours, trab in minutes, ins/emb/catalogo).
 * `fotos` is the ordered photo list — external URLs or /api/media/{hash}; the
 * first one becomes the cover.
 */
public record ProductRequest(
        @JsonProperty("nome") String name,
        @JsonProperty("cat") String categoryCode,
        @JsonProperty("tam") String size,
        ProductStatus status,
        @JsonProperty("obs") String observations,
        @JsonProperty("desc") String description,
        @JsonProperty("fotos") List<String> photos,
        @JsonProperty("origem") String origin,
        @JsonProperty("impressora") String printer,
        @JsonProperty("filamento") String filament,
        @JsonProperty("gram") BigDecimal grams,
        @JsonProperty("tempo") BigDecimal printTimeHours,
        @JsonProperty("trab") BigDecimal laborMinutes,
        @JsonProperty("ins") BigDecimal supplies,
        @JsonProperty("emb") BigDecimal packaging,
        @JsonProperty("catalogo") BigDecimal catalogPrice,
        // storefront / SEO
        String slug,
        @JsonProperty("prazo") Integer leadTimeDays,
        @JsonProperty("ordem") Integer sortOrder,
        String material,
        @JsonProperty("dimPeca") String partDimensions,
        @JsonProperty("embPeso") BigDecimal packageWeight,
        @JsonProperty("embDim") String packageDimensions,
        @JsonProperty("publicado") Boolean published,
        @JsonProperty("destaque") Boolean featured,
        @JsonProperty("descLonga") String longDescription,
        @JsonProperty("metaDesc") String metaDescription,
        @JsonProperty("licenca") String license) {
}
