package dev.m2g2.simao.model.automation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Automation extends BaseModel {

    public String  name;

    public ActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    public Map<String, Object> metadata = new HashMap<>();

    @Enumerated(EnumType.STRING)
    public Periodicity periodicity;

    public LocalTime executionTime;

    public enum Periodicity {
        DAILY,
        CUSTOM;

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

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    public LocalTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalTime executionTime) {
        this.executionTime = executionTime;
    }
}
