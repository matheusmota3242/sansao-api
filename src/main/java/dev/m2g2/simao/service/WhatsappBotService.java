package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.chat.AutomationChatResponse;
import dev.m2g2.simao.dto.chat.ChatResponse;
import dev.m2g2.simao.dto.chat.NoteChatResponse;
import dev.m2g2.simao.dto.chat.PurchaseChatResponse;
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

    @Value("${application.purchase-group-id}")
    private String purchaseGroupId;

    private final WahaClientService wahaClientService;
    private final TaskService taskService;
    private final AutomationService automationService;
    private final NoteService noteService;
    private final PurchaseService purchaseService;
    private final ChatRecordService chatRecordService;

    public WhatsappBotService(WahaClientService wahaClientService, TaskService taskService, AutomationService automationService, NoteService noteService, PurchaseService purchaseService, ChatRecordService chatRecordService) {
        this.wahaClientService = wahaClientService;
        this.taskService = taskService;
        this.automationService = automationService;
        this.noteService = noteService;
        this.purchaseService = purchaseService;
        this.chatRecordService = chatRecordService;
    }

    public void receiveMessage(WahaRequest requestDto) {
        if (!isValid(requestDto, purchaseGroupId)) {
            return;
        }
        String chatId = resolveChatId(requestDto);
        String incomingMessage = requestDto.payload().body().replace("#", "").trim();
        String reply = null;
        if (isRelatedToChatRecord(incomingMessage)) {
            reply = retrieveReplyFromChatRecord(incomingMessage).orElse(null);
        }

        if (reply == null) {
            reply = isPurchaseGroup(chatId)
                    ? matchPurchaseCommand(incomingMessage)
                    : matchOwnerCommand(incomingMessage);
        }
        if (reply == null) {
            return;
        }
        reply = format(reply);
        WahaSendMessageResponse responseDto = wahaClientService.sendText(new WahaSendMessageRequest(chatId, reply));
    }

    private String matchOwnerCommand(String incomingMessage) {
        return Stream.of(
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

    private String matchPurchaseCommand(String incomingMessage) {
        return Stream.of(
                    ChatType.showPurchaseMenuIf(incomingMessage),
                    ChatType.showPurchaseInlineTemplateIf(incomingMessage),
                    purchaseService.createInteractionIf(incomingMessage),
                    purchaseService.listIf(incomingMessage),
                    purchaseService.updateInteractionIf(incomingMessage),
                    purchaseService.deleteIf(incomingMessage),
                    purchaseService.createInlineIf(incomingMessage)
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean isPurchaseGroup(String chatId) {
        return purchaseGroupId != null && !purchaseGroupId.isBlank() && purchaseGroupId.equals(chatId);
    }

    private String resolveChatId(WahaRequest requestDto) {
        // Mensagem de outra pessoa (ex.: membro do grupo de compras): responder no chat de origem.
        if (!Boolean.TRUE.equals(requestDto.payload().fromMe())) {
            return requestDto.payload().from();
        }
        String to = requestDto.payload().to();
        if (to == null || to.isBlank()) {
            return ownerPhone + "@c.us";
        }
        return to;
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

                    if (chatResponse instanceof PurchaseChatResponse purchaseChatResponse) {
                        if (purchaseChatResponse.updateId() == null)
                            purchaseService.create(purchaseChatResponse.purchase());
                        else
                            purchaseService.update(purchaseChatResponse.updateId(), purchaseChatResponse.purchase());
                    }
                }
            }
        }
        chatRecordService.update(record);
        return Optional.ofNullable(reply);
    }

}
