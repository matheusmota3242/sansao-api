package dev.m2g2.simao.dto.chat;

public sealed interface ChatResponse permits TaskChatResponse, AutomationChatResponse, NoteChatResponse, PurchaseChatResponse, OrderChatResponse, TrackerChatResponse {
    String text();
    boolean completed();
}
