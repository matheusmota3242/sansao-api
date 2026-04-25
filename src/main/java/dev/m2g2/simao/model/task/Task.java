package dev.m2g2.simao.model.task;

import dev.m2g2.simao.model.BaseModel;
import dev.m2g2.simao.model.automation.schedule.ScheduleConfig;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
public class Task extends BaseModel {

    private String description;
    private LocalDateTime scheduledAt;
    private boolean completed = false;
    @JdbcTypeCode(SqlTypes.JSON)
    private ScheduleConfig scheduleConfig;

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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public void setScheduleConfig(ScheduleConfig scheduleConfig) {
        this.scheduleConfig = scheduleConfig;
    }
}
