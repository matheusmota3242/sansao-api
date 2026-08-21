package dev.m2g2.simao.model.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Product lifecycle status. Serialized to/from the lowercase codes the ARGILA
 * LAB frontend uses ("ativo"/"dev"/"off"); persisted as the enum name.
 */
public enum ProductStatus {
    ATIVO("ativo"),
    DEV("dev"),
    OFF("off");

    private final String code;

    ProductStatus(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ProductStatus from(String value) {
        if (value == null)
            return ATIVO;
        for (ProductStatus s : values()) {
            if (s.code.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                return s;
        }
        throw new IllegalArgumentException("Status inválido: " + value);
    }
}
