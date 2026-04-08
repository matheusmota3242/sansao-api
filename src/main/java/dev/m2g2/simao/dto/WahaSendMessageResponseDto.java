package dev.m2g2.simao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaSendMessageResponseDto(Key key) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Key(String id) {}
}
