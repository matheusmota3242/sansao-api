package dev.m2g2.simao.model.automation.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DailySchedule implements ScheduleConfig {

    private LocalTime time;

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public LocalDateTime getNextExecutionAt() {
        if (time == null) return null;
        LocalDate day = LocalDate.now();
        if (time.isBefore(LocalTime.now()))
            day = day.plusDays(1);

        return LocalDateTime.of(day, time);
    }
}
