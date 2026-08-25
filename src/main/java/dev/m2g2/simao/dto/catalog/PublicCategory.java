package dev.m2g2.simao.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Categoria como a vitrine a lista: código, nome e slug para a URL. */
public record PublicCategory(
        String id,
        @JsonProperty("nome") String name,
        String slug) {
}
