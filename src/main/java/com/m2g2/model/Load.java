package com.m2g2.model;

import com.m2g2.enums.MassUnity;
import jakarta.persistence.*;

@Entity
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private MassUnity unity;

    private Integer quantity;

    private Float weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MassUnity getUnity() {
        return unity;
    }

    public void setUnity(MassUnity unity) {
        this.unity = unity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Load{" +
                "id=" + id +
                ", unity=" + unity +
                ", quantity=" + quantity +
                ", weight=" + weight +
                '}';
    }
}
