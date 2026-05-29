package dev.m2g2.simao.model.chat.automation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.AutomationDTO;
import dev.m2g2.simao.dto.chat.AutomationChatResponse;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.automation.schedule.DailySchedule;
import dev.m2g2.simao.model.automation.schedule.SpecificDateSchedule;
import dev.m2g2.simao.model.automation.schedule.WeeklyCustomSchedule;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import dev.m2g2.simao.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dev.m2g2.simao.dto.chat.AutomationChatResponse.*;

@JsonTypeName("create_automation")
public class CreateAutomationInteraction extends Interaction<AutomationDTO> {

    private static final String INVALID_FORMAT_ERROR_MESSAGE = """
                    Formato inválido. Tente novamente.
                    
                    Diário
                    _Ex: 12:00_
                    
                    Data específica
                    _Ex: 01/01/2000_
                    
                    Semanal customizado
                    _Ex: 2 12:00,4 14:00 (segunda às 12h e quarta às 14h)_
                    """;
    public static final String AUTOMATION_CREATED_SUCCESSFULLY = "Automação cadastrada com sucesso!";

    public CreateAutomationInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Nome da automação:"),
                new Step("""
                        Tipo da automação:
                        
                        %s
                        """.formatted(ActionType.showAutomationActionTypes())),
                new Step("Destinatário:"),
                new Step("""
                        Data e hora:

                        Diário
                        _Ex: 12:00_

                        Data específica
                        _Ex: 01/01/2000 12:00_

                        Semanal customizado
                        _Ex: SEG 12:00, QUA 14:00 (segunda às 12h e quarta às 14h)_
                        """),
                new Step("É recorrente? (S/N)")
                ));
        this.data = new AutomationDTO();
    }

    @Override
    public AutomationChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_AUTOMATION.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        if (this.steps.getFirst().equals(getCurrentStep()))
            return executeNameStep(value);

        if (this.steps.get(1).equals(getCurrentStep()))
            return executeAutomationTypeStep(value);


        if (this.steps.get(2).equals(getCurrentStep())) {
            if (ActionType.SEND_TASK_REMEMBERING.equals(this.data.getActionType()))
                return executeTaskAssociationStep(value);

            if (ActionType.SEND_SIMPLE_TEXT.equals(this.data.getActionType()))
                return executeMessageStep(value);
        }

        if (this.steps.get(3).equals(getCurrentStep()))
            return executeToStep(value);

        return executeScheduleConfigStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Criação de automação cancelada.";
    }

    private AutomationChatResponse executeNameStep(String value) {
        if (value == null || value.isBlank())
            value = "Undefined";
        this.data.setName(value);
        this.steps.getFirst().setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeAutomationTypeStep(String value) {
        try {
            Optional<ActionType> actionType = ActionType.fromIndex(value == null ? 0 : Integer.parseInt(value));
            if (actionType.isPresent()) {
                this.data.setActionType(actionType.get());
                this.steps.get(1).setCompleted(true);
                if (ActionType.SEND_TASK_REMEMBERING.equals(actionType.get())) {
                    this.steps.add(2, new Step("Id da tarefa:"));
                }

                if (ActionType.SEND_SIMPLE_TEXT.equals(actionType.get())) {
                    this.steps.add(2, new Step("Mensagem:"));
                }
            }
        } catch (NumberFormatException e) {
            return error("Tipo de ação inválida. Tente novamente.");
        }
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeTaskAssociationStep(String value) {
        try {
            Long taskId = Long.parseLong(value);
            this.data.getMetadata().put("taskId", taskId);
            this.steps.get(2).setCompleted(true);
        } catch (NumberFormatException e) {
            return error("Valor do id inválido. Precisa ser um número. Tente novamente.");
        }
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeMessageStep(String value) {
        this.data.getMetadata().put("message", value);
        this.steps.get(2).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeToStep(String value) {
        this.data.getMetadata().put("to", value);
        this.steps.get(3).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeScheduleConfigStep(String value) {
        if (this.data.getScheduleConfig() != null)
            return executeRecurrenceStep(value);

        if (value == null || value.isBlank())
            return error(INVALID_FORMAT_ERROR_MESSAGE);

        LocalTime time = null;
        try {
            time = LocalTime.parse(value);
            DailySchedule dailySchedule = new DailySchedule();
            dailySchedule.setTime(time);
            this.data.setScheduleConfig(dailySchedule);
        } catch (Exception _) {}

        LocalDateTime dateTime = null;
        if (time == null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                dateTime = LocalDateTime.parse(value, formatter);
                SpecificDateSchedule specificDateSchedule = new SpecificDateSchedule();
                specificDateSchedule.setDate(dateTime);
                this.data.setScheduleConfig(specificDateSchedule);
            } catch (Exception _) {}
        }

        if (time == null && dateTime == null) {
            try {
                List<WeeklyCustomSchedule.DayTime> dayTimes = Arrays.stream(value.split(","))
                        .map(part -> {
                            String[] pieces = part.trim().split(" ");
                            int day;
                            LocalTime dayTime;
                            try {
                                day = DateTimeUtil.getDayOfWeekIndex(pieces[0]);
                                dayTime = LocalTime.parse(pieces[1]);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(INVALID_FORMAT_ERROR_MESSAGE, e);
                            }
                            if (day < 1 || day > 7)
                                throw new IllegalArgumentException("Dia precisa estar no intervalo 1-7. Tente novamente.");
                            return new WeeklyCustomSchedule.DayTime(day, dayTime);
                        }).toList();
                WeeklyCustomSchedule weeklyCustomSchedule = new WeeklyCustomSchedule();
                weeklyCustomSchedule.setDays(dayTimes);
                this.data.setScheduleConfig(weeklyCustomSchedule);
            } catch (Exception e) {
                return error(e.getMessage());
            }
        }

        getCurrentStep().setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private AutomationChatResponse executeRecurrenceStep(String value) {
        if (value == null || value.isBlank())
            return error("Responda S para sim ou N para não.");
        String trimmed = value.trim().toUpperCase();
        if (!trimmed.equals("S") && !trimmed.equals("N"))
            return error("Responda S para sim ou N para não.");
        this.data.setRecurrent(trimmed.equals("S"));
        return success(AUTOMATION_CREATED_SUCCESSFULLY, data);
    }
}
