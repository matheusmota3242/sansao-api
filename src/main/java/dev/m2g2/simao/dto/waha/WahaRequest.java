package dev.m2g2.simao.dto.waha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WahaRequest(Payload payload) {

    /**
     * `participant` is WAHA's sender id for a message posted inside a group
     * (mirrors the underlying WhatsApp protocol's participant/author field on
     * group messages; absent — null — on direct messages). Unverified against
     * a live WAHA payload as of this writing; confirm the field name before
     * treating this as final.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String body, Boolean fromMe, String from, String to, String source, String participant) {}
}
