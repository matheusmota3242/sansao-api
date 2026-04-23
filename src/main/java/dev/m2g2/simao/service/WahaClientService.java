package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.waha.WahaSendMessageRequest;
import dev.m2g2.simao.dto.waha.WahaSendMessageResponse;
import dev.m2g2.simao.dto.waha.WahaSessionConfig;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "http://localhost:3000/api", accept = "application/json", contentType = "application/json")
public interface WahaClientService {

    @PostExchange("/sendText")
    WahaSendMessageResponse sendText(@RequestBody WahaSendMessageRequest requestDto);

    @PutExchange("/sessions/{sessionId}")
    WahaSessionConfig config(@PathVariable String sessionId, @RequestBody WahaSessionConfig config);
}
