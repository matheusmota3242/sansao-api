package dev.m2g2.simao.dto.waha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaRequest(Payload payload) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String body, Boolean fromMe, String from, String to, String source) {}
}
