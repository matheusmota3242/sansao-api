package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.TrackerDTO;

public record TrackerChatResponse(String text, boolean completed, TrackerDTO tracker)
        implements ChatResponse {

    public static TrackerChatResponse proceed(String text) {
        return new TrackerChatResponse(text, false, null);
    }

    public static TrackerChatResponse error(String text) {
        return new TrackerChatResponse(text, false, null);
    }

    public static TrackerChatResponse success(String text, TrackerDTO tracker) {
        return new TrackerChatResponse(text, true, tracker);
    }
}
