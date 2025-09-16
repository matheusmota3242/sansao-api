package dev.m2g2.dto;

import dev.m2g2.model.Exercise;

import java.util.List;

public record WorkoutDTO(String start, String end, String description, List<Exercise> exercises) {
}
