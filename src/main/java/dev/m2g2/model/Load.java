package dev.m2g2.model;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class Load {

    private Integer repetitions;

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

    public String toCustomizedString() {
        return "• "+repetitions+" x "+weight+"\n";
    }

    @Override
    public String toString() {
        return "\n\tLoad{" +
                "\n\trepetitions=" + repetitions +
                ",\n\t weight=" + weight +
                "}\n";
    }
}
