package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.m2g2.simao.model.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String sku,
        @JsonProperty("cat") String categoryCode,
        @JsonProperty("catNome") String categoryName,
        Integer num,
        @JsonProperty("tam") String size,
        @JsonProperty("nome") String name,
        @JsonProperty("desc") String description,
        ProductStatus status,
        @JsonProperty("obs") String observations,
        @JsonProperty("gram") BigDecimal grams,
        @JsonProperty("tempo") BigDecimal printTimeHours,
        @JsonProperty("trab") BigDecimal laborMinutes,
        @JsonProperty("ins") BigDecimal supplies,
        @JsonProperty("emb") BigDecimal packaging,
        @JsonProperty("catalogo") BigDecimal catalogPrice,
        @JsonProperty("tempoExato") boolean exactTime,
        @JsonProperty("foto") String photo,
        @JsonProperty("fotos") List<String> photos,
        @JsonProperty("origem") String origin,
        @JsonProperty("impressora") String printer,
        @JsonProperty("filamento") String filament,
        // storefront / SEO
        String slug,
        @JsonProperty("prazo") Integer leadTimeDays,
        @JsonProperty("ordem") Integer sortOrder,
        String material,
        @JsonProperty("dimPeca") String partDimensions,
        @JsonProperty("embPeso") BigDecimal packageWeight,
        @JsonProperty("embDim") String packageDimensions,
        @JsonProperty("publicado") boolean published,
        @JsonProperty("destaque") boolean featured,
        @JsonProperty("descLonga") String longDescription,
        @JsonProperty("metaDesc") String metaDescription,
        @JsonProperty("licenca") String license,
        @JsonProperty("custo") CostBreakdown cost) {
}
