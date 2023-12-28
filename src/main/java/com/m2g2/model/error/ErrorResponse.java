package com.m2g2.model.error;

import org.springframework.util.ErrorHandler;
import org.springframework.util.StringUtils;

public class ErrorResponse {

    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
