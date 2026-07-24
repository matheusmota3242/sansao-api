package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.PurchaseDTO;

public record PurchaseChatResponse(String text, boolean completed, PurchaseDTO purchase, Long updateId)
        implements ChatResponse {

    public static PurchaseChatResponse proceed(String text) {
        return new PurchaseChatResponse(text, false, null, null);
    }

    public static PurchaseChatResponse error(String text) {
        return new PurchaseChatResponse(text, false, null, null);
    }

    public static PurchaseChatResponse created(String text, PurchaseDTO purchase) {
        return new PurchaseChatResponse(text, true, purchase, null);
    }

    public static PurchaseChatResponse updated(String text, PurchaseDTO purchase, Long updateId) {
        return new PurchaseChatResponse(text, true, purchase, updateId);
    }
}
