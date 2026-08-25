package dev.m2g2.simao.enums;

public enum Role {
    ADMIN("Administrador"),
    OPERATOR("Operador");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Custo, margem e parâmetros de custo são só do ADMIN. O OPERATOR mexe em
     * produtos, pedidos e clientes sem enxergar quanto a loja ganha em cada um.
     */
    public boolean canSeeCosts() {
        return this == ADMIN;
    }
}
