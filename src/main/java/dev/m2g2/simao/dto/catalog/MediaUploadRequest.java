package dev.m2g2.simao.dto.catalog;

/** A data: URI coming from the browser (already compressed client-side). */
public record MediaUploadRequest(String dataUri) {
}
