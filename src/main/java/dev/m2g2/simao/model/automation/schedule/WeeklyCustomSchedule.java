package dev.m2g2.simao.model.automation.schedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class WeeklyCustomSchedule implements ScheduleConfig {

    private List<DayTime> days;

    public List<DayTime> getDays() {
        return days;
    }

    public void setDays(List<DayTime> days) {
        this.days = days;
    }

    public static class DayTime {
        private DayOfWeek day;
        private LocalTime time;

        public DayTime(int day, LocalTime time) {
            this.day = DayOfWeek.of(day);
            this.time = time;
        }

        public DayOfWeek getDay() {
            return day;
        }

        public void setDay(DayOfWeek day) {
            this.day = day;
        }

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    @Override
    public LocalDateTime getNextExecutionAt() {
        if (days == null || days.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return days.stream()
                .map(dayTime -> {
                    DayOfWeek targetDay = dayTime.getDay();
                    LocalTime targetTime = dayTime.getTime();

                    LocalDateTime candidate = now
                            .with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay))
                            .withHour(targetTime.getHour())
                            .withMinute(targetTime.getMinute())
                            .withSecond(0)
                            .withNano(0);

                    if (candidate.isBefore(now)) {
                        candidate = candidate.plusWeeks(1);
                    }

                    return candidate;
                })
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }
}
