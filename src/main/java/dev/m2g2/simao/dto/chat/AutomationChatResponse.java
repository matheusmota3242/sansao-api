package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.AutomationDTO;

public record AutomationChatResponse(String text, boolean completed, AutomationDTO automation) implements ChatResponse {
    public static AutomationChatResponse proceed() {
        return new AutomationChatResponse(null, false, null);
    }

    public static AutomationChatResponse proceed(String text) {
        return new AutomationChatResponse(text, false, null);
    }

    public static AutomationChatResponse error(String text) {
        return new AutomationChatResponse(text, false, null);
    }

    public static AutomationChatResponse success(String text, AutomationDTO automation) {
        return new AutomationChatResponse(text, true, automation);
    }
}
