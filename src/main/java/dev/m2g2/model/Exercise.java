package dev.m2g2.model;

import java.util.List;
import java.util.Optional;

public class Exercise {

    private String description;

    private List<Load> loads;

    private String comment;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Load> getLoads() {
        return loads;
    }

    public void setLoads(List<Load> loads) {
        this.loads = loads;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toCustomizedString() {
        StringBuilder loadsStringBuilder = new StringBuilder();
        if (loads != null) {
            loads.forEach(load ->
                loadsStringBuilder.append(load.toCustomizedString())
            );
        }
        return description
                .concat("\n")
                .concat(Optional.ofNullable(comment).orElse(""))
                .concat("\n")
                .concat(loadsStringBuilder.toString())
                .concat("\n");
    }

    @Override
    public String toString() {
        return "\n\tExercise{" +
                "\n\tdescription='" + description + '\'' +
                ",\n\t loads=" + loads +
                ",\n\t comment='" + comment + '\'' +
                "}\n";
    }
}
