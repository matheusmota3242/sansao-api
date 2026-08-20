package dev.m2g2.simao.model.tracker;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Tracker extends BaseModel {

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
