package dev.m2g2.simao.model.automation.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DailySchedule.class, name = "DAILY"),
        @JsonSubTypes.Type(value = WeeklyCustomSchedule.class, name = "WEEKLY_CUSTOM"),
        @JsonSubTypes.Type(value = SpecificDateSchedule.class, name = "SPECIFIC_DATE")
})
public interface ScheduleConfig {
    @JsonIgnore
    LocalDateTime getNextExecutionAt();
}