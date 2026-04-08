package dev.m2g2.simao.controller;

import dev.m2g2.simao.dto.WahaRequestDto;
import dev.m2g2.simao.service.WhatsappBotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp")
public class WhatsappController {

    private final WhatsappBotService whatsappBotService;

    public WhatsappController(WhatsappBotService whatsappBotService) {
        this.whatsappBotService = whatsappBotService;
    }

    @PostMapping("/message")
    public void receiveMessage(@RequestBody WahaRequestDto requestDto) {
        whatsappBotService.receiveMessage(requestDto);
    }
}
