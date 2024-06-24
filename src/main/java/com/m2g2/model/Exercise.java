package com.m2g2.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class Exercise {

    @NotNull
    private String description;

    private List<Load> loads;

    private String comment;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Load> getLoads() {
        return loads;
    }

    public void setLoads(List<Load> loads) {
        this.loads = loads;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "description='" + description + '\'' +
                ", loads=" + loads +
                ", comment='" + comment + '\'' +
                '}';
    }
}
