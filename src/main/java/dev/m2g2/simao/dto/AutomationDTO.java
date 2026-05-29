package dev.m2g2.simao.dto;

import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.automation.schedule.ScheduleConfig;

import java.util.HashMap;
import java.util.Map;

public class AutomationDTO {

    private String name;
    private ActionType actionType;
    private Map<String, Object> metadata = new HashMap<>();
    private ScheduleConfig scheduleConfig;
    private boolean recurrent;

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

    public boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }
}
