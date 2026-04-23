package dev.m2g2.simao.dto.waha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaSendMessageResponse(Key key) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Key(String id) {}
}
