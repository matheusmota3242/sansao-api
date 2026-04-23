package dev.m2g2.simao.model.automation;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Execution extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "automation_id")
    public Automation automation;

    public LocalDateTime executedAt;

    public Automation getAutomation() {
        return automation;
    }

    public void setAutomation(Automation automation) {
        this.automation = automation;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
