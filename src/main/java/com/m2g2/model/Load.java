package com.m2g2.model;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class Load {

    @NotNull
    private Integer repetitions;

    @NotNull
    private BigDecimal weight;

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }


    @Override
    public String toString() {
        return "Load{" +
                "repetitions=" + repetitions +
                ", weight=" + weight +
                '}';
    }
}
