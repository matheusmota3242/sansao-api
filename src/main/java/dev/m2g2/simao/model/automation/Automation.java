package dev.m2g2.simao.model.automation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.BaseModel;
import dev.m2g2.simao.model.automation.schedule.ScheduleConfig;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Automation extends BaseModel {

    private String  name;

    private ActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    public Map<String, Object> metadata = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private ScheduleConfig scheduleConfig;

    private LocalDateTime nextExecutionAt;

    private LocalDateTime executedAt;

    private boolean recurrent;

    public enum Periodicity {
        DAILY,
        WEEKLY_CUSTOM,
        DATETIME;

        @JsonIgnore
        public String fromString(String value) {
            for (Periodicity periodicity : Periodicity.values()) {
                if (periodicity.name().equalsIgnoreCase(value)) {
                    return periodicity.name();
                }
            }
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public void setScheduleConfig(ScheduleConfig scheduleConfig) {
        this.scheduleConfig = scheduleConfig;
    }

    public LocalDateTime getNextExecutionAt() {
        return nextExecutionAt;
    }

    public void setNextExecutionAt(LocalDateTime nextExecutionAt) {
        this.nextExecutionAt = nextExecutionAt;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void updateNextExecutionAtOrInactivate() {
        if (recurrent) {
            this.nextExecutionAt = this.scheduleConfig.getNextExecutionAt();
        } else {
            this.active = false;
        }
    }
}
