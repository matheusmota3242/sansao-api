package dev.m2g2.simao.model.chat.task;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.TaskDTO;
import dev.m2g2.simao.dto.chat.TaskChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import dev.m2g2.simao.model.task.TaskScheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonTypeName("create_task")
public class CreateTaskInteraction extends Interaction<TaskDTO> {

    public CreateTaskInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Descrição da tarefa:"),
                new Step("""
                        Data e hora da tarefa:
                        Ex: 01/01/1900 12:00
                        """),
                new Step("""
                        Periodicidade da tarefa (opcional):
                        
                        0 - Todos os dias
                        
                        1 0...6 - Semana customizada
                        Ex: 1 0,2,4
                        """)
        ));
        this.data = new TaskDTO();
    }

    @Override
    public TaskChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_TASK.getValue())) {
            return new TaskChatResponse(this.steps.getFirst().getDescription(), false, null);
        }

        if (this.steps.getFirst().equals(getCurrentStep())) {
            return executeDescriptionStep(value);
        }

        if (this.steps.get(1).equals(getCurrentStep())) {
            return executeDatetimeStep(value);
        }

        return executeTaskPeriodicity(value);
    }

    private TaskChatResponse executeDescriptionStep(String value) {
        this.data.setDescription(value);
        this.steps.getFirst().setCompleted(true);
        return new TaskChatResponse(getCurrentStep().getDescription(), false, null);
    }

    private TaskChatResponse executeDatetimeStep(String value) {
        Set<String> tomorrowEquivalents = Set.of("tomorrow", "amanha");
        LocalDateTime scheduledAt = LocalDateTime.now();
        if (tomorrowEquivalents.contains(value.toLowerCase()))
            scheduledAt = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        else  {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                scheduledAt = LocalDateTime.parse(value, formatter);
            } catch (Exception _) {}
        }
        this.data.setScheduledAt(scheduledAt);
        this.steps.get(1).setCompleted(true);
        return new TaskChatResponse(getCurrentStep().getDescription(), false, null);
    }

    private TaskChatResponse executeTaskPeriodicity(String value) {
        final String TASK_CREATED_MESSAGE = "Tarefa cadastrada com sucesso!";
        if (value == null || (!value.startsWith("0") && !value.startsWith("1")))
            return new TaskChatResponse(TASK_CREATED_MESSAGE, true, this.data);
        if (value.equals("0")) {
            this.data.setPeriodicity(TaskScheduler.Periodicity.DAILY);
            return new TaskChatResponse(TASK_CREATED_MESSAGE, true, this.data);
        }

        String[] chunks = value.split(" ");
        String[] daysOfweek = chunks[1].split(",");
        if (daysOfweek.length > 7) {
            return new TaskChatResponse("Dias da semana inválidos. Tente novamente.", false, null);
        }

        for (String day : daysOfweek) {
            try {
                int number = Integer.parseInt(day);
                if (number < 0 || number > 6) {
                    return new TaskChatResponse("Dia da semana inválido. Tente novamente.", false, null);
                }
            } catch (NumberFormatException e) {
                return new TaskChatResponse("Dia da semana inválido. Tente novamente.", false, null);
            }
        }

        if ("1".equals(chunks[0])) {
            this.data.setPeriodicity(TaskScheduler.Periodicity.WEEKLY_CUSTOM);
            this.data.setCustomSchedule(Map.of("daysOfWeek", daysOfweek));
        }
        return new TaskChatResponse(TASK_CREATED_MESSAGE, true, this.data);
    }

}
