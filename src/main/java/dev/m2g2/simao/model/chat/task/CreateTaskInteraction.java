package dev.m2g2.simao.model.chat.task;

import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.Task;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CreateTaskInteraction extends Interaction<Task> {

    public CreateTaskInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Task description:") {
                    @Override
                    public Optional<Object> execute(String value) {
                        data.setDescription(value);
                        steps.getFirst().setCompleted(true);
                        return Optional.empty();
                    }
                },
                new Step("Date and time:") {
                    @Override
                    public Optional<Object> execute(String value) {
                        LocalDateTime scheduledAt = LocalDateTime.now();
                        if ("#tomorrow".equalsIgnoreCase(value))
                            scheduledAt = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
                        else  {
                            try {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                scheduledAt = LocalDateTime.parse(value, formatter);
                            } catch (Exception _) {}
                        }
                        data.setScheduledAt(scheduledAt);
                        steps.get(1).setCompleted(true);
                        return Optional.of(Pair.of("Task created!", data));
                    }
                }
        ));
        this.data = new Task();
    }

    @Override
    public Object processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_TASK.getValue())) {
            return this.steps.getFirst().getDescription();
        }
        return super.processInput(value);
    }

}
