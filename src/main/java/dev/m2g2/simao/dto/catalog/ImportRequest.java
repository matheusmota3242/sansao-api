package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Full argilalab.json project payload. Extra top-level fields (app, versao,
 * salvoEm) are ignored. `midia` maps a media key to a data: URI or URL, and
 * `loja` carries the storefront config (v4).
 *
 * The JSON keys stay Portuguese on purpose: this mirrors an external file
 * format that already exists on disk, so it is not ours to rename.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImportRequest(
        CostParametersDTO params,
        @JsonProperty("cats") Map<String, String> categories,
        @JsonProperty("midia") Map<String, String> media,
        @JsonProperty("loja") StoreConfigDTO store,
        @JsonProperty("produtos") List<ImportProduct> products) {
}
