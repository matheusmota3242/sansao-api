package dev.m2g2.simao.model.task;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Task extends BaseModel {

    private String description;
    // Optional due/deadline date. Recurrence lives on Automation, not here.
    private LocalDateTime scheduledAt;
    private boolean completed = false;

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
}
