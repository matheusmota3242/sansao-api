package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.OrderDTO;

public record OrderChatResponse(String text, boolean completed, OrderDTO order, Long updateId)
        implements ChatResponse {

    public static OrderChatResponse proceed(String text) {
        return new OrderChatResponse(text, false, null, null);
    }

    public static OrderChatResponse error(String text) {
        return new OrderChatResponse(text, false, null, null);
    }

    public static OrderChatResponse created(String text, OrderDTO order) {
        return new OrderChatResponse(text, true, order, null);
    }

    public static OrderChatResponse updated(String text, OrderDTO order, Long updateId) {
        return new OrderChatResponse(text, true, order, updateId);
    }
}
