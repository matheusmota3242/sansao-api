package dev.m2g2.simao.service.automation;

import dev.m2g2.simao.annotation.AutomationAction;
import dev.m2g2.simao.dto.WahaSendMessageRequestDto;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.service.AutomationService;
import dev.m2g2.simao.service.WahaClientService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AutomationAction(actionType = ActionType.SEND_SIMPLE_TEXT)
public class SendSimpleTextActionService implements ActionBaseService {

    private final WahaClientService wahaClientService;
    private final AutomationService automationService;

    public SendSimpleTextActionService(WahaClientService wahaClientService, AutomationService automationService) {
        this.wahaClientService = wahaClientService;
        this.automationService = automationService;
    }

    @Override
    public void execute(Long automationId) {
        Optional<Automation> automation = automationService.getById(automationId);
        if (automation.isEmpty()) {
            return;
        }
        try {
            String to = automation.get().getMetadata().get("to").toString();
            String text = automation.get().getMetadata().get("text").toString();
            wahaClientService.sendText(new WahaSendMessageRequestDto(to, text));
        } catch (Exception _) {}
    }
}
