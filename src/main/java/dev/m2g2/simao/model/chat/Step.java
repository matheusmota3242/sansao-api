package dev.m2g2.simao.model.chat;

public class Step {
    private String description;
    private boolean completed = false;

    public Step(String description) {
        this.description = description;
    }

    public Step() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
