package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.WahaRequestDto;
import dev.m2g2.simao.dto.WahaSendMessageRequestDto;
import dev.m2g2.simao.dto.WahaSendMessageResponseDto;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.enums.ResponseType;
import dev.m2g2.simao.model.chat.ChatRecord;
import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class WhatsappBotService {

    private final WahaClientService wahaClientService;
    private final TaskService taskService;
    private final AutomationService automationService;
    private final ChatRecordService chatRecordService;

    public WhatsappBotService(WahaClientService wahaClientService, TaskService taskService, AutomationService automationService, ChatRecordService chatRecordService) {
        this.wahaClientService = wahaClientService;
        this.taskService = taskService;
        this.automationService = automationService;
        this.chatRecordService = chatRecordService;
    }

    public void receiveMessage(WahaRequestDto requestDto) {
        if (requestDto.payload() == null ||
                requestDto.payload().body() == null ||
                !Boolean.TRUE.equals(requestDto.payload().fromMe()) ||
                "api".equals(requestDto.payload().source())) {
            return;
        }
        String message = requestDto.payload().body().trim();
        String reply = retrieveReplyFromNotCompletedChatRecord(message).orElse(null);
        if (reply == null) {
            reply = Stream.of(
                        ChatType.showMenuIf(message),
                        taskService.createInteractionIf(message),
                        taskService.listIf(message),
                        taskService.deleteIf(message),
                        taskService.completeTaskIf(message),
                        automationService.createInteractionIf(message),
                        automationService.listIf(message),
                        automationService.deleteIf(message)
                    )
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (reply == null) {
            return;
        }

        reply = "\uD83E\uDD16 "+reply;
        WahaSendMessageResponseDto responseDto = wahaClientService.sendText(new WahaSendMessageRequestDto("558499607700@c.us", reply));
    }

    @Transactional
    private Optional<String> retrieveReplyFromNotCompletedChatRecord(String message) {
        String reply = null;
        ChatRecord record = chatRecordService.getLastNotCompletedChatRecord().orElse(null);
        if (record != null) {
            if (message.equalsIgnoreCase(ChatType.CANCEL.getValue())) {
                record.setActive(false);
            } else {
                Object data = record.getInteraction().processInput(message);
                LocalDateTime now = LocalDateTime.now();
                record.setUpdatedAt(now);
                if (data != null) {
                    if (data instanceof ResponseType) {
                        if (data.equals(ResponseType.SHOW_TASKS)) {
                            reply = taskService.listIf(ChatType.LIST_TASKS.getValue());
                            return Optional.of(reply);
                        }
                    } else if (data instanceof String) {
                        reply = (String) data;
                    }

                    if (reply == null)
                        reply = taskService.createIf(data, record);

                    if (reply == null)
                        reply = automationService.createIf(data, record);
                }
            }
            chatRecordService.update(record);
        }
        return Optional.ofNullable(reply);
    }
}
