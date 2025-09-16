package dev.m2g2.context;

import dev.m2g2.model.Workout;
import dev.m2g2.repository.WorkoutRepository;

import java.util.List;

public class GetWorkoutContext extends AbstractContext {

    private final WorkoutRepository repository;

    public GetWorkoutContext(WorkoutRepository repository) {
        this.repository = repository;
        this.stepIndex = 0;
        this.steps.add(
                new Step(getAllWorkouts(), null)
        );
    }

    private String getAllWorkouts() {
        String response;
        List<Workout> workouts = repository.findAll();
        if (workouts.isEmpty()) {
            response = "Nenhum treino cadastrado até o momento.";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Workout workout : workouts) {
                stringBuilder.append(workout.toCustomizedString());
            }
            response = stringBuilder.toString();
        }
        return response;
    }
}
