package dev.m2g2.simao.service.automation.action;

import dev.m2g2.simao.annotation.AutomationAction;
import dev.m2g2.simao.dto.automation.ExecutionResult;
import dev.m2g2.simao.dto.waha.WahaSendMessageRequest;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.service.WahaClientService;
import dev.m2g2.simao.util.ChatbotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static dev.m2g2.simao.dto.automation.ExecutionResult.failure;
import static dev.m2g2.simao.dto.automation.ExecutionResult.ok;

@Service
@AutomationAction(actionType = ActionType.SEND_SIMPLE_TEXT)
public class SendSimpleTextActionService implements ActionBaseService {

    private static final Logger log = LoggerFactory.getLogger(SendSimpleTextActionService.class);

    private final WahaClientService wahaClientService;

    public SendSimpleTextActionService(WahaClientService wahaClientService      ) {
        this.wahaClientService = wahaClientService;
    }

    @Override
    public ExecutionResult execute(Automation automation) {
        String to = automation.getMetadata().get("to").toString();
        String text = ChatbotUtil.format(automation.getMetadata().get("message").toString());
        try {
            wahaClientService.sendText(new WahaSendMessageRequest(to, text));
            return ok();
        } catch (Exception e) {
            log.error("Error sending message '{}' to '{}'", e.getMessage(), to, e);
            return failure(e.getMessage());
        }
    }
}
