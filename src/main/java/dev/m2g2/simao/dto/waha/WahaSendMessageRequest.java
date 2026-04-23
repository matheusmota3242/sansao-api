package dev.m2g2.simao.dto.waha;

public record WahaSendMessageRequest(String chatId, String text, String session) {

    public WahaSendMessageRequest(String chatId, String text) {
        this(chatId, text, "default");
    }
}
