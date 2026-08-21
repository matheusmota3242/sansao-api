package dev.m2g2.simao.dto.catalog;

/** url is what the frontend stores on the product photo list. */
public record MediaResponse(String hash, String url, String contentType, int bytes) {
}
