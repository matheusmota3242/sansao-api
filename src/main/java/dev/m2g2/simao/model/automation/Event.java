package dev.m2g2.simao.model.automation;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Event extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "automation_id")
    public Automation automation;

    public Automation getAutomation() {
        return automation;
    }

    public void setAutomation(Automation automation) {
        this.automation = automation;
    }
}
