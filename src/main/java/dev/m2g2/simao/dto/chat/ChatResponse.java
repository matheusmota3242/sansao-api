package dev.m2g2.simao.dto.chat;

public sealed interface ChatResponse permits TaskChatResponse, AutomationChatResponse {
    String text();
    boolean completed();
}
