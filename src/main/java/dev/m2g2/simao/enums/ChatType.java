package dev.m2g2.simao.enums;

public enum ChatType {
    MENU("@menu", "Show the menu"),
    CANCEL("@cancel", "Inactivate last not completed chat"),
    CREATE_TASK("@ctask", "Create new task"),
    LIST_TASKS("@ltask", "List all tasks"),
    DELETE_TASK("@dtask", "Delete task from ID"),
    EXECUTE_TASK("@etask", "Mark task as completed"),
    CREATE_AUTOMATION("@cauto", "Create new automation"),
    LIST_AUTOMATIONS("@lauto", "List all automations"),
    DELETE_AUTOMATION("@dauto", "Delete automation from ID"),;

    private final String value;
    private final String description;

    ChatType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static String showMenuIf(String message) {
        if (message.equalsIgnoreCase(ChatType.MENU.getValue())) {
            StringBuilder menu = new StringBuilder("Here is the menu:\n\n");
            for (ChatType chatType : ChatType.values()) {
                menu.append("*").append(chatType.getValue()).append("*").append(" - ").append(chatType.getDescription()).append("\n");
            }
            return menu.toString();
        }
        return null;
    }
}
