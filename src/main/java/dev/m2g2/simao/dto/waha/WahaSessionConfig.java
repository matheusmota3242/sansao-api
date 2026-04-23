package dev.m2g2.simao.dto.waha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaSessionConfig(Config config) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Config(
            Noweb noweb,
            Integer retries,
            List<Webhook> webhooks,
            @JsonProperty("markOnline")
            Boolean markOnline) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Noweb(Store store) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Store(
            Boolean enabled,
            Boolean fullSync) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Webhook(
            String url,
            List<String> events) {}
}