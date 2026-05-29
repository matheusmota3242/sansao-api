package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.NoteDTO;

public record NoteChatResponse(String text, boolean completed, NoteDTO note)
        implements ChatResponse {

    public static NoteChatResponse proceed(String text) {
        return new NoteChatResponse(text, false, null);
    }

    public static NoteChatResponse error(String text) {
        return new NoteChatResponse(text, false, null);
    }

    public static NoteChatResponse success(String text, NoteDTO note) {
        return new NoteChatResponse(text, true, note);
    }
}
