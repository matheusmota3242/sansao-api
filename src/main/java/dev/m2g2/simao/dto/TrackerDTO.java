package dev.m2g2.simao.dto;

import java.math.BigDecimal;

public class TrackerDTO {

    private String name;
    private String keyword;
    private String unit;
    private BigDecimal dailyGoal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(BigDecimal dailyGoal) {
        this.dailyGoal = dailyGoal;
    }
}
