package dev.m2g2.simao.service.automation.action;

import dev.m2g2.simao.annotation.AutomationAction;
import dev.m2g2.simao.dto.automation.ExecutionResult;
import dev.m2g2.simao.dto.waha.WahaSendMessageRequest;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.task.Task;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.service.TaskService;
import dev.m2g2.simao.service.WahaClientService;
import dev.m2g2.simao.util.ChatbotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static dev.m2g2.simao.dto.automation.ExecutionResult.failure;
import static dev.m2g2.simao.dto.automation.ExecutionResult.ok;

@Service
@AutomationAction(actionType = ActionType.SEND_TASK_REMEMBERING)
public class SendTaskRememberingActionService implements ActionBaseService {

    private final static Logger log = LoggerFactory.getLogger(SendTaskRememberingActionService.class);

    private final WahaClientService wahaClientService;
    private final TaskService taskService;

    public SendTaskRememberingActionService(WahaClientService wahaClientService,  TaskService taskService) {
        this.wahaClientService = wahaClientService;
        this.taskService = taskService;
    }


    @Override
    public ExecutionResult execute(Automation automation) {
        Long taskId = (Long) automation.getMetadata().get("taskId");
        Optional<Task> task = taskService.getById(taskId);
        if (task.isEmpty()) {
            String error = "Could not execute automation %d. Task with id %d not found".formatted(automation.getId(), taskId);
            return failure(error);
        }
        String to = automation.getMetadata().get("to").toString();
        String text = """
                    Lembrete de tarefa!
                    
                    %s - %s""".formatted(task.get().getDescription(), task.get().getScheduledAt());
        try {
            wahaClientService.sendText(new WahaSendMessageRequest(to, ChatbotUtil.format(text)));
            return ok();
        } catch (Exception e) {
            log.error("Error sending remembering '{}' to '{}'", e.getMessage(), to, e);
            return failure(e.getMessage());
        }
    }
}
