package dev.m2g2.simao.enums;

import java.util.ArrayList;
import java.util.List;

public enum ChatType {
    MENU("@menu", "Show the menu", false),
    CANCEL("@cancel", "Inactivate last not completed chat", false),
    CREATE_TASK("@ctask", "Create new task", true),
    LIST_TASKS("@ltask", "List all tasks", false),
    DELETE_TASK("@dtask", "Delete task from ID", false),
    EXECUTE_TASK("@etask", "Mark task as completed", false),
    CREATE_AUTOMATION("@cauto", "Create new automation", true),
    LIST_AUTOMATIONS("@lauto", "List all automations", false),
    DELETE_AUTOMATION("@dauto", "Delete automation from ID", false),
    CREATE_NOTE("@cnote", "Create new note", true),
    LIST_NOTES("@lnote", "List all notes", false),
    DELETE_NOTE("@dnote", "Delete note from ID", false);

    private final String value;
    private final String description;
    private final Boolean requireInteraction;

    ChatType(String value, String description, Boolean requireInteraction) {
        this.value = value;
        this.description = description;
        this.requireInteraction = requireInteraction;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getRequireInteraction() {
        return requireInteraction;
    }

    public static String showMenuIf(String message) {
        if (message.equalsIgnoreCase(ChatType.MENU.getValue())) {
            StringBuilder menu = new StringBuilder("""
                    Aqui está o menu:
                    
                    """);
            for (ChatType chatType : ChatType.values()) {
                menu.append("*").append(chatType.getValue()).append("*").append(" - ").append(chatType.getDescription()).append("\n");
            }
            return menu.toString();
        }
        return null;
    }

    public static List<ChatType> allByInteractionRequirement(boolean requireInteraction) {
        List<ChatType> chatTypes = new ArrayList<>();
        for (ChatType chatType : ChatType.values()) {
            if (chatType.getRequireInteraction() == requireInteraction) {
                chatTypes.add(chatType);
            }
        }
        return chatTypes;
    }
}
