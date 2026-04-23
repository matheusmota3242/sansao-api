package dev.m2g2.simao.service.automation;

import dev.m2g2.simao.AutomationActionFactory;
import dev.m2g2.simao.dto.AutomationDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.automation.CreateAutomationInteraction;
import dev.m2g2.simao.repository.AutomationRepository;
import dev.m2g2.simao.service.ChatRecordService;
import dev.m2g2.simao.service.InteractionBaseService;
import dev.m2g2.simao.service.automation.action.ActionBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AutomationService implements InteractionBaseService {

    private final AutomationRepository repository;
    private final ChatRecordService chatRecordService;
    private final AutomationActionFactory actionFactory;

    private static Logger log = LoggerFactory.getLogger(AutomationService.class.getName());

    public AutomationService(AutomationRepository repository, ChatRecordService chatRecordService, AutomationActionFactory actionFactory) {
        this.repository = repository;
        this.chatRecordService = chatRecordService;
        this.actionFactory = actionFactory;
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
            return createAutomationInteraction.processInput(incomingMessage).text();
        }
        return null;
    }

    public Automation create(AutomationDTO automationDTO) {
        Automation automation = new Automation();
        automation.setName(automationDTO.getName());
        automation.setActionType(automationDTO.getActionType());
        automation.setMetadata(automationDTO.getMetadata());
        automation.setActive(true);
        LocalDateTime now = LocalDateTime.now();
        automation.setCreatedAt(now);
        automation.setUpdatedAt(now);
        automation.setScheduleConfig(automationDTO.getScheduleConfig());
        automation.setNextExecutionAt(automationDTO.getScheduleConfig().getNextExecutionAt());
        return repository.save(automation);
    }

    @Override
    public String listIf(String incomingMessage) {
        String reply = null;
        if (incomingMessage.equalsIgnoreCase(ChatType.LIST_AUTOMATIONS.getValue())) {
            List<Automation> automations = repository.findAllByActiveTrue();
            if (automations.isEmpty()) {
                reply = "Você não possui automações!";
            } else {
                StringBuilder builder = new StringBuilder("""
                        Aqui estão suas automações:
                        
                        """);
                for (int i = 0; i < automations.size(); i++) {
                    builder.append("%d - %s".formatted(automations.get(i).getId(), automations.get(i).getName()));
                    if (i < automations.size() - 1)
                        builder.append("\n");
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
            reply = String.format("Automação com id %d removida!", taskId);
        }
        return reply;
    }

    public void executeBatch() {
        Pageable limit = PageRequest.of(0, 10, Sort.by("nextExecutionAt").ascending());
        List<Automation> automations = repository.findAllByActiveTrueAndNextExecutionAtBefore(LocalDateTime.now(), limit);
        for (Automation automation : automations) {
            try {
                ActionBaseService actionService = actionFactory.getActionServiceByType(automation.getActionType());
                actionService.execute(automation);
                automation.updateNextExecutionAtOrInactivate();
                automation.setUpdatedAt(LocalDateTime.now());
                repository.save(automation);

            } catch (Exception e) {
                log.error("Error executing automation: {}", automation.getName(), e);
            }
        }
    }
}
