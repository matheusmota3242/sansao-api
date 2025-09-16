package dev.m2g2.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Document
public class Workout {
    @MongoId
    private String id;
    @NotNull
    private String description;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime start;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime end;
    private List<Exercise> exercises;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String toCustomizedString() {
        StringBuilder exercicesStringBuilder = new StringBuilder();
        if (exercises != null) {
            for (int i = 0; i < exercises.size(); i++) {
                exercicesStringBuilder.append(i + 1).append(" - ").append(exercises.get(i).toCustomizedString()).append("\n");
            }

        }
        return "--------------------"
                .concat("\n")
                .concat(description).concat(" - ").concat(Optional.ofNullable(start).map(LocalDateTime::toString).orElse(""))
                .concat("\n")
                .concat("\n")
                .concat(exercicesStringBuilder.toString())
                .concat("\n");

    }

    @Override
    public String toString() {
        return "Workout{" +
                "\n description='" + description + '\'' +
                ",\n start=" + start +
                ",\n end=" + end +
                ",\n exercises=" + exercises +
                "}\n";
    }
}
