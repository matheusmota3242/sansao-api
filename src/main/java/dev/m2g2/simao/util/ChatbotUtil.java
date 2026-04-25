package dev.m2g2.simao.util;

import dev.m2g2.simao.dto.waha.WahaRequest;

import static dev.m2g2.simao.enums.ChatType.CANCEL;

public class ChatbotUtil {

    public static String format(String text) {
        return "\uD83E\uDD16 " + text;
    }

    public static boolean isRelatedToChatRecord(String incomingMessage) {
        return CANCEL.getValue().equals(incomingMessage) || !incomingMessage.startsWith("@");
    }

    public static boolean isValid(WahaRequest requestDto) {
        return requestDto.payload() != null &&
                requestDto.payload().body() != null &&
                (requestDto.payload().body().startsWith("#") || requestDto.payload().body().startsWith("@")) &&
                Boolean.TRUE.equals(requestDto.payload().fromMe()) &&
                !"api".equals(requestDto.payload().source());
    }
}
