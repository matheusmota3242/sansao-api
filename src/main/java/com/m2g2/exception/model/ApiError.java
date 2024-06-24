package com.m2g2.exception.model;

public class ApiError {

    private String description;

    public ApiError(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
