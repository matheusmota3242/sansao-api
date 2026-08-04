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

    public static boolean isValid(WahaRequest requestDto, String purchaseGroupId) {
        if (requestDto.payload() == null || requestDto.payload().body() == null) {
            return false;
        }
        WahaRequest.Payload payload = requestDto.payload();
        boolean isCommand = payload.body().startsWith("#") || payload.body().startsWith("@");
        boolean notFromApi = !"api".equals(payload.source());
        boolean fromOwner = Boolean.TRUE.equals(payload.fromMe());
        boolean fromPurchaseGroup = purchaseGroupId != null &&
                !purchaseGroupId.isBlank() &&
                purchaseGroupId.equals(payload.from());
        return isCommand && notFromApi && (fromOwner || fromPurchaseGroup);
    }
}
