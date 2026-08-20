package dev.m2g2.simao.enums;

public enum OrderStatus {
    WAITING("Aguardando"),
    RUNNING("Imprimindo"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * WAITING and RUNNING are the states that occupy a position in the printing
     * queue; COMPLETED and CANCELLED have left it.
     */
    public boolean isInQueue() {
        return this == WAITING || this == RUNNING;
    }

    /**
     * Resolves a status from user input, accepting either the enum name or the
     * Portuguese label, case-insensitively. Returns null when nothing matches.
     */
    public static OrderStatus fromInput(String value) {
        if (value == null || value.isBlank())
            return null;
        String normalized = value.trim();
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized) || status.label.equalsIgnoreCase(normalized))
                return status;
        }
        return null;
    }
}
