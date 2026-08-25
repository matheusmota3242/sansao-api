package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * One product as it appears in an argilalab.json export (frontend field names).
 * Photos may come as a single `foto` (v1) or a `fotos` list (v4); entries can be
 * external URLs, data: URIs, or keys into the export's `midia` map.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImportProduct(
        @JsonProperty("cat") String categoryCode,
        Integer num,
        @JsonProperty("tam") String size,
        @JsonProperty("nome") String name,
        @JsonProperty("desc") String description,
        ProductStatus status,
        @JsonProperty("gram") BigDecimal grams,
        @JsonProperty("tempo") BigDecimal printTimeHours,
        @JsonProperty("trab") BigDecimal laborMinutes,
        @JsonProperty("ins") BigDecimal supplies,
        @JsonProperty("emb") BigDecimal packaging,
        @JsonProperty("catalogo") BigDecimal catalogPrice,
        @JsonProperty("tempoExato") Boolean exactTime,
        @JsonProperty("foto") String photo,
        @JsonProperty("fotos") List<String> photos,
        @JsonProperty("origem") String origin,
        @JsonProperty("obs") String observations,
        @JsonProperty("impressora") String printer,
        @JsonProperty("filamento") String filament,
        // v4 storefront / SEO
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
