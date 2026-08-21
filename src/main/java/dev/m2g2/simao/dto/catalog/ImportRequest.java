package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Full argilalab.json project payload. Extra top-level fields (app, versao,
 * salvoEm) are ignored. `midia` maps a media key to a data: URI or URL, and
 * `loja` carries the storefront config (v4).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImportRequest(
        CostParametersDTO params,
        Map<String, String> cats,
        Map<String, String> midia,
        StoreConfigDTO loja,
        List<ImportProduct> produtos) {
}
