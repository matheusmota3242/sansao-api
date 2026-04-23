package dev.m2g2.simao.model.automation.schedule;

import java.time.LocalDateTime;

public class SpecificDateSchedule implements ScheduleConfig {

    private LocalDateTime date;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public LocalDateTime getNextExecutionAt() {
        return date;
    }
}
