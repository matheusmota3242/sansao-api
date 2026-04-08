package dev.m2g2.simao.model.automation;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class AutomationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "automation_id", nullable = false)
    private Automation automation;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Automation getAutomation() {
        return automation;
    }

    public void setAutomation(Automation automation) {
        this.automation = automation;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
