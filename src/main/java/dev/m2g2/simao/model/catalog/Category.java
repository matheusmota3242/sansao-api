package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Entity;

@Entity
public class Category extends BaseModel {

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
