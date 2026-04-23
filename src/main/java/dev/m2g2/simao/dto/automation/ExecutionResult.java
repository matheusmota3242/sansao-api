package dev.m2g2.simao.dto.automation;

public record ExecutionResult(boolean success, String error) {

    public static ExecutionResult failure(String error) {
        return new ExecutionResult(false, error);
    }

    public static ExecutionResult ok() {
        return new ExecutionResult(true, null);
    }
}
