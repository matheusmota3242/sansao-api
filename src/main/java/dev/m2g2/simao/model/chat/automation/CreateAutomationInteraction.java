package dev.m2g2.simao.model.chat.automation;

import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.automation.Automation;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CreateAutomationInteraction extends Interaction<Automation> {

    public CreateAutomationInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Automation name:") {
                    @Override
                    public Optional<Object> execute(String value) {
                        if (value == null || value.isBlank())
                            value = "Undefined";
                        data.setName(value);
                        steps.getFirst().setCompleted(true);
                        return Optional.empty();
                    }
                },
                new Step("Select an automation type:\n\n"+ ActionType.showAutomationActionTypes()) {
                    @Override
                    public Optional<Object> execute(String value) {
                        try {
                            Optional<ActionType> actionType = ActionType.fromIndex(value == null ? 0 : Integer.parseInt(value));
                            if (actionType.isPresent()) {
                                data.setActionType(actionType.get());
                                steps.get(1).setCompleted(true);
                                if (ActionType.SEND_TASK_REMEMBERING.equals(actionType.get())) {
                                    steps.add(2, new Step("Task ID:") {
                                        @Override
                                        public Optional<Object> execute(String value) {
                                            try {
                                                Long taskId = Long.parseLong(value);
                                                data.setMetadata(Map.of("taskId", taskId));
                                            } catch (NumberFormatException e) {
                                                return Optional.of("Invalid task ID. Must be a number. Type again.");
                                            }
                                            return Optional.empty();
                                        }
                                    });
                                    steps.removeLast();
                                }
                            }
                        } catch (NumberFormatException e) {
                            return Optional.of("Invalid action type. Type again.");
                        }
                        return Optional.empty();
                    }
                },
                new Step("Periodicity") {
                    @Override
                    public Optional<Object> execute(String value) {
                        Automation.Periodicity periodicity = Automation.Periodicity.DAILY;
                        data.setPeriodicity(Automation.Periodicity.DAILY);
                        steps.get(2).setCompleted(true);
                        return Optional.empty();
                    }
                },
                new Step("To:") {
                    @Override
                    public Optional<Object> execute(String value) {
                        data.setMetadata(Map.of("to", value));
                        steps.get(3).setCompleted(true);
                        if (ActionType.SEND_TASK_REMEMBERING.equals(data.getActionType())) {
                            return Optional.of(Pair.of("Automation created!", data));
                        }
                        return Optional.empty();
                    }
                },
                new Step("Message:") {
                    @Override
                    public Optional<Object> execute(String value) {
                        data.setMetadata(Map.of("message", value));
                        steps.get(4).setCompleted(true);
                        return Optional.of(Pair.of("Automation created!", data));
                    }
                }
        ));
        this.data = new Automation();
    }

    @Override
    public Object processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_AUTOMATION.getValue())) {
            return this.steps.getFirst().getDescription();
        }
        return super.processInput(value);
    }
}
