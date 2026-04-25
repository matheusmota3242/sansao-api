package dev.m2g2.simao.enums;

import java.util.Optional;

public enum ActionType {
    SEND_SIMPLE_TEXT("Enviar mensagem de texto"),
    SEND_TASK_REMEMBERING("Lembrar de tarefa");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<ActionType> fromIndex(int index) {
        for (ActionType actionType : ActionType.values()) {
            if (actionType.ordinal() == index) {
                return Optional.of(actionType);
            }
        }
        return Optional.empty();
    }

    public static String showAutomationActionTypes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ActionType.values().length; i++) {
            sb.append("%d - %s".formatted(values()[i].ordinal(), values()[i].getDescription()));
            if (i < ActionType.values().length - 1)
                sb.append("\n");
        }
        return sb.toString();
    }
}
