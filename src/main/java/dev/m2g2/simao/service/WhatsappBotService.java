package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.WahaRequestDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WhatsappBotService {

    private final TaskService taskService;

    public WhatsappBotService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void receiveMessage(WahaRequestDto requestDto) {
        String message = Optional.ofNullable(requestDto)
                .map(WahaRequestDto::payload)
                .map(WahaRequestDto.Payload::body)
                .orElse(null);
        if (message == null) {
            return;
        }
        String value;
        switch (message.toLowerCase()) {
            case "#menu":
                value = "Esse é o menu:\n" +
                        "#task - Agendar uma tarefa";
                break;
            case "#task":
                value = "Agendar uma tarefa";
                break;
            default:
                return;
        }
        System.out.println(value);

    }
}
