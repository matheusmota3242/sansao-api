package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.WahaSendMessageRequestDto;
import dev.m2g2.simao.dto.WahaSendMessageResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "http://localhost:3000/api", accept = "application/json", contentType = "application/json")
public interface WahaClientService {

    @PostExchange("/sendText")
    WahaSendMessageResponseDto sendText(
            @RequestBody WahaSendMessageRequestDto requestDto);
}
