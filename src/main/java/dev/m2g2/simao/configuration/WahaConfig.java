package dev.m2g2.simao.configuration;

import dev.m2g2.simao.dto.waha.WahaSendMessageRequest;
import dev.m2g2.simao.dto.waha.WahaSessionConfig;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.service.ChatRecordService;
import dev.m2g2.simao.service.WahaClientService;
import dev.m2g2.simao.util.ChatbotUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class WahaConfig {

    private final Logger log = LoggerFactory.getLogger(WahaConfig.class);

    @Value("${application.host}")
    private String host;

    @Value("${application.owner-phone}")
    private String ownerPhone;

    @Value("${waha.webhook-secret}")
    private String webhookSecret;

    private final WahaClientService wahaClientService;
    private final ChatRecordService chatRecordService;

    public WahaConfig(WahaClientService wahaClientService, ChatRecordService chatRecordService) {
        this.wahaClientService = wahaClientService;
        this.chatRecordService = chatRecordService;
    }

    @PostConstruct
    private void postConstruct() {
        int attempt = 1;
        int maxAttempts = 3;
        long delay = 1000;
        while (attempt <= maxAttempts) {
            try {
                wahaClientService.config("default", getWahaSessionConfigRequestDto());
                log.info("Waha session configured successfully");
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting to send Waha health message", e);
                    }
                    wahaClientService.sendText(new WahaSendMessageRequest(ownerPhone, ChatbotUtil.format("Simão Bot is now online!")));
                    ChatRecord record = chatRecordService.getLastNotCompletedChatRecord().orElse(null);
                    if (record != null) {
                        String stepDescription = record.getInteraction().getCurrentStep().getDescription();
                        wahaClientService.sendText(new WahaSendMessageRequest(ownerPhone, ChatbotUtil.format(stepDescription)));
                    }
                });
                return;
            } catch (ResourceAccessException | HttpServerErrorException e) {
                log.error("Error {} configuring Waha session", attempt, e);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {}
                attempt++;
                delay *= 2;
            }
        }
    }

    private WahaSessionConfig getWahaSessionConfigRequestDto() {
        List<WahaSessionConfig.Webhook> webhooks = List.of(new WahaSessionConfig.Webhook(host + "/whatsapp/message", List.of("message", "message.any"), List.of(new WahaSessionConfig.CustomHeader("X-Webhook-Secret", webhookSecret))));
        WahaSessionConfig.Noweb noweb = new WahaSessionConfig.Noweb(new WahaSessionConfig.Store(false, false));
        WahaSessionConfig.Config config = new WahaSessionConfig.Config(noweb, 2, webhooks, false);
        return new WahaSessionConfig(config);
    }
}
