package com.m2g2.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ExerciseType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    @OneToMany
    private List<TrainingItem> trainingItens;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TrainingItem> getTrainingItens() {
        return trainingItens;
    }

    public void setTrainingItens(List<TrainingItem> trainingItens) {
        this.trainingItens = trainingItens;
    }
}
