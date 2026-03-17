package dev.m2g2.simao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaRequestDto(Payload payload) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String body) {}
}
