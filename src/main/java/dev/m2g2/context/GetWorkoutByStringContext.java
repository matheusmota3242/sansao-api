package dev.m2g2.context;

import dev.m2g2.model.Workout;
import dev.m2g2.repository.WorkoutRepository;

import java.util.List;
import java.util.Optional;

public class GetWorkoutByStringContext extends AbstractContext {

    private final WorkoutRepository repository;
    public GetWorkoutByStringContext(WorkoutRepository repository) {
        this.repository = repository;
        this.stepIndex = 0;
        this.steps.add(
                new Step("Digite a palavra", this::processString)
        );
    }

    private Optional<Object> processString(String text) {
        Optional<Object> response;
        List<Workout> workouts = repository.findAllThatContains(text);
        if (workouts.isEmpty()) {
            response = Optional.of("Nenhum treino cadastrado até o momento.");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Workout workout : workouts) {
                stringBuilder.append(workout.toCustomizedString());
            }
            response = Optional.of(stringBuilder.toString());
        }
        return response;
    }
}
