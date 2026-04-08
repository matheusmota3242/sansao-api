package dev.m2g2.simao.service;

import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.Task;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.automation.CreateAutomationInteraction;
import dev.m2g2.simao.repository.AutomationRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AutomationService implements InteractionBaseService {

    private final AutomationRepository repository;
    private final ChatRecordService chatRecordService;

    public AutomationService(AutomationRepository repository, ChatRecordService chatRecordService) {
        this.repository = repository;
        this.chatRecordService = chatRecordService;
    }

    public Optional<Automation> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public String createInteractionIf(String incomingMessage) {
        if (incomingMessage.equalsIgnoreCase(ChatType.CREATE_AUTOMATION.getValue())) {
            CreateAutomationInteraction createAutomationInteraction = new CreateAutomationInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(createAutomationInteraction);
            chatRecordService.create(record);
            return createAutomationInteraction.processInput(incomingMessage).toString();
        }
        return null;
    }

    @Override
    public String createIf(Object pairCandidate, ChatRecord record) {
        String reply = null;
        if (pairCandidate instanceof Pair<?,?> replyAndAutomation) {
            record.setCompleted(true);
            reply = (String) replyAndAutomation.getFirst();
            if (replyAndAutomation.getSecond() instanceof Automation automation) {
                LocalDateTime now = LocalDateTime.now();
                automation.setCreatedAt(now);
                automation.setUpdatedAt(now);
                automation.setActive(true);
                repository.save(automation);
            }
        }
        return reply;

    }

    @Override
    public String listIf(String incomingMessage) {
        String reply = null;
        if (incomingMessage.equalsIgnoreCase(ChatType.LIST_TASKS.getValue())) {
            List<Automation> automations = repository.findAllByActiveTrue();
            if (automations.isEmpty()) {
                reply = "You don't have any automations yet!";
            } else {
                StringBuilder builder = new StringBuilder("Here are your automations:\n\n");
                for (Automation automation : automations) {
                    builder.append(automation.getId())
                            .append(" - ")
                            .append(automation.getName())
                            .append(" (")
                            .append(automation.getPeriodicity().name())
                            .append(")")
                            .append("\n");
                }
                reply = builder.toString();
            }
        }
        return reply;
    }

    @Override
    public String deleteIf(String incomingMessage) {
        String reply = null;
        if (incomingMessage.toLowerCase().startsWith("@dauto")) {
            String[] parts = incomingMessage.split(" ");
            Long taskId = Long.parseLong(parts[1]);
            repository.deleteById(taskId);
            reply = String.format("Automation with id %d deleted!", taskId);
        }
        return reply;
    }
}
