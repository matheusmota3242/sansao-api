package dev.m2g2.simao.dto;

import dev.m2g2.simao.model.task.TaskScheduler;

import java.time.LocalDateTime;
import java.util.Map;

public class TaskDTO {
    private String description;
    private LocalDateTime scheduledAt;
    private Map<String, Object> customSchedule;
    TaskScheduler.Periodicity periodicity;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Map<String, Object> getCustomSchedule() {
        return customSchedule;
    }

    public void setCustomSchedule(Map<String, Object> customSchedule) {
        this.customSchedule = customSchedule;
    }

    public TaskScheduler.Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(TaskScheduler.Periodicity periodicity) {
        this.periodicity = periodicity;
    }
}
