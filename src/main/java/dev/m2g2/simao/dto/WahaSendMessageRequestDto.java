package dev.m2g2.simao.dto;

public record WahaSendMessageRequestDto(String chatId, String text, String session) {

    public WahaSendMessageRequestDto(String chatId, String text) {
        this(chatId, text, "default");
    }
}
