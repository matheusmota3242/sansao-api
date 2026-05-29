package dev.m2g2.simao.controller;

import dev.m2g2.simao.dto.waha.WahaRequest;
import dev.m2g2.simao.service.WhatsappBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/whatsapp")
public class WhatsappController {

    private final WhatsappBotService whatsappBotService;

    @Value("${waha.webhook-secret}")
    private String webhookSecret;

    public WhatsappController(WhatsappBotService whatsappBotService) {
        this.whatsappBotService = whatsappBotService;
    }

    @PostMapping("/message")
    public void receiveMessage(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody WahaRequest requestDto) {
        if (!webhookSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        whatsappBotService.receiveMessage(requestDto);
    }
}
