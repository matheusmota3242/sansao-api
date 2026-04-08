package dev.m2g2.simao.service.automation;

import dev.m2g2.simao.annotation.AutomationAction;
import dev.m2g2.simao.dto.WahaSendMessageRequestDto;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.Task;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.service.AutomationService;
import dev.m2g2.simao.service.TaskService;
import dev.m2g2.simao.service.WahaClientService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AutomationAction(actionType = ActionType.SEND_TASK_REMEMBERING)
public class SendTaskRememberingActionService implements ActionBaseService {

    private final WahaClientService wahaClientService;
    private final AutomationService automationService;
    private final TaskService taskService;

    public SendTaskRememberingActionService(WahaClientService wahaClientService, AutomationService automationService, TaskService taskService) {
        this.wahaClientService = wahaClientService;
        this.automationService = automationService;
        this.taskService = taskService;
    }

    @Override
    public void execute(Long automationId) {
        Optional<Automation> automation = automationService.getById(automationId);
        if (automation.isEmpty()) {
            return;
        }
        Long taskId = (Long) automation.get().getMetadata().get("taskId");
        Optional<Task> task = taskService.getById(taskId);
        if (task.isEmpty()) {
            return;
        }
        try {
            String to = automation.get().getMetadata().get("to").toString();
            wahaClientService.sendText(new WahaSendMessageRequestDto(to, "Remember task:\n\n"+task.get().getDescription()+" - "+task.get().getScheduledAt()));
        } catch (Exception _) {}
    }
}
