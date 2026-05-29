package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.chat.AutomationChatResponse;
import dev.m2g2.simao.dto.chat.ChatResponse;
import dev.m2g2.simao.dto.chat.NoteChatResponse;
import dev.m2g2.simao.dto.chat.TaskChatResponse;
import dev.m2g2.simao.dto.waha.WahaRequest;
import dev.m2g2.simao.dto.waha.WahaSendMessageRequest;
import dev.m2g2.simao.dto.waha.WahaSendMessageResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.service.automation.AutomationService;
import dev.m2g2.simao.util.ChatbotUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.m2g2.simao.enums.ChatType.CANCEL;
import static dev.m2g2.simao.enums.ChatType.showMenuIf;
import static dev.m2g2.simao.util.ChatbotUtil.*;

@Service
public class WhatsappBotService {

    @Value("${application.owner-phone}")
    private String ownerPhone;

    private final WahaClientService wahaClientService;
    private final TaskService taskService;
    private final AutomationService automationService;
    private final NoteService noteService;
    private final ChatRecordService chatRecordService;

    public WhatsappBotService(WahaClientService wahaClientService, TaskService taskService, AutomationService automationService, NoteService noteService, ChatRecordService chatRecordService) {
        this.wahaClientService = wahaClientService;
        this.taskService = taskService;
        this.automationService = automationService;
        this.noteService = noteService;
        this.chatRecordService = chatRecordService;
    }

    public void receiveMessage(WahaRequest requestDto) {
        if (!isValid(requestDto)) {
            return;
        }
        String incomingMessage = requestDto.payload().body().replace("#", "").trim();
        String reply = null;
        if (isRelatedToChatRecord(incomingMessage)) {
            reply = retrieveReplyFromChatRecord(incomingMessage).orElse(null);
        }

        if (reply == null) {
            reply = Stream.of(
                        showMenuIf(incomingMessage),
                        taskService.createInteractionIf(incomingMessage),
                        taskService.listIf(incomingMessage),
                        taskService.deleteIf(incomingMessage),
                        taskService.completeTaskIf(incomingMessage),
                        automationService.createInteractionIf(incomingMessage),
                        automationService.listIf(incomingMessage),
                        automationService.deleteIf(incomingMessage),
                        noteService.createInteractionIf(incomingMessage),
                        noteService.listIf(incomingMessage),
                        noteService.deleteIf(incomingMessage)
                    )
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        if (reply == null) {
            return;
        }
        reply = format(reply);
        WahaSendMessageResponse responseDto = wahaClientService.sendText(new WahaSendMessageRequest(ownerPhone + "@c.us", reply));
    }

    @Transactional
    private Optional<String> retrieveReplyFromChatRecord(String message) {
        String reply = null;
        ChatRecord record = chatRecordService.getLastNotCompletedChatRecord().orElse(null);
        if (record == null) {
            return Optional.empty();
        }
        if (ChatType.allByInteractionRequirement(true)
                .stream()
                .anyMatch(chatType -> chatType.getValue().equalsIgnoreCase(message)))
            return Optional.empty();

        if (message.equalsIgnoreCase(ChatType.CANCEL.getValue())) {
            record.setActive(false);
            reply = record.getInteraction().cancelMessage();
        } else {
            ChatResponse chatResponse = record.getInteraction().processInput(message);
            LocalDateTime now = LocalDateTime.now();
            record.setUpdatedAt(now);
            if (chatResponse != null) {
                reply = chatResponse.text();
                if (chatResponse.completed()) {
                    record.setActive(false);
                    if (chatResponse instanceof TaskChatResponse taskChatResponse)
                        taskService.create(taskChatResponse.task());

                    if (chatResponse instanceof AutomationChatResponse automationChatResponse)
                        automationService.create(automationChatResponse.automation());

                    if (chatResponse instanceof NoteChatResponse noteChatResponse)
                        noteService.create(noteChatResponse.note());
                }
            }
        }
        chatRecordService.update(record);
        return Optional.ofNullable(reply);
    }

}
