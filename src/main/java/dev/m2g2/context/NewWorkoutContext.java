package dev.m2g2.context;

import dev.m2g2.model.Exercise;
import dev.m2g2.model.Load;
import dev.m2g2.model.Workout;
import dev.m2g2.repository.WorkoutRepository;
import dev.m2g2.utils.Constants;
import dev.m2g2.utils.GreetingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.m2g2.utils.Constants.SUCCESS;
import static dev.m2g2.utils.Constants.UNDOING_NAMES;

public class NewWorkoutContext extends AbstractContext {

    private static final Logger logger = LoggerFactory.getLogger(NewWorkoutContext.class);

    private final WorkoutRepository repository;

    private Workout workout;

    private int exerciseIndex;

    public NewWorkoutContext(WorkoutRepository repository) {
        this.repository = repository;
        this.stepIndex = 0;
        this.exerciseIndex = 1;
        this.steps.addAll(List.of(
                new AbstractContext.Step(
                        "Treino de que?",
                        this::processWorkoutDescriptionAndInit),
                new AbstractContext.Step(
                        "Diga o exercício 1",
                        this::processExerciseDescription),
                new AbstractContext.Step(
                        """
                        Tá pegando quanto?
                        """,
                        this::processLoadWeight),
                new AbstractContext.Step(
                        """
                        Quantas repetições?
                        """,
                        this::processLoadRepetitions),
                new AbstractContext.Step(
                        """
                        Algum comentário,"""+" "+GreetingsUtils.getRandomicNickname()+"?",
                        this::processExerciseComment),
                new AbstractContext.Step(
                        """
                        E agora?
                        
                        [REP] Mais uma repetição?
                        [EX]  Iniciar outro exercício?
                        
                        """,
                        this::closeOrAddOneMoreExercise)
        ));
    }

    public Optional<Object> processWorkoutDescriptionAndInit(String text) {
        if (workout == null) {
            workout = new Workout();
            workout.setStart(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime());
        }
        workout.setDescription(text.trim());
        stepIndex++;
        return Optional.empty();
    }

    public Optional<Object> processExerciseDescription(String text) {
        if (!UNDOING_NAMES.contains(text.toLowerCase())) {
            Exercise exercise = new Exercise();
            exercise.setDescription(text.trim());
            if (CollectionUtils.isEmpty(workout.getExercises())) {
                workout.setExercises(new ArrayList<>());
            }
            workout.getExercises().add(exercise);
            stepIndex++;
            incrementExerciseIndexMessage();
        } else {
            exerciseIndex--;
            stepIndex--;
        }
        return Optional.empty();
    }

    public Optional<Object> processLoadWeight(String text) {
        if (!UNDOING_NAMES.contains(text.toLowerCase())) {
            try {
                BigDecimal weight = BigDecimal.valueOf(Double.parseDouble(getOnlyNumbersFromText(text)));
                Load load = new Load();
                load.setWeight(weight);
                if (CollectionUtils.isEmpty(workout.getExercises().getLast().getLoads())) {
                    workout.getExercises().getLast().setLoads(new ArrayList<>());
                    workout.getExercises().getLast().getLoads().add(load);
                } else {
                    workout.getExercises().getLast().getLoads().add(load);
                }
                stepIndex++;
            } catch (NumberFormatException e) {
                logger.error(e.getMessage());
            }
        } else {
            workout.getExercises().removeLast();
        }
        return Optional.empty();
    }

    public Optional<Object> processLoadRepetitions(String text) {
        if (!UNDOING_NAMES.contains(text.toLowerCase())) {
            Integer repetitions = Integer.parseInt(text);
            workout.getExercises().getLast().getLoads().getLast().setRepetions(repetitions);
            stepIndex++;
        } else {
            stepIndex--;
        }
        return Optional.empty();
    }

    public Optional<Object> processExerciseComment(String text) {
        if (!"não".equalsIgnoreCase(text) && !"n".equalsIgnoreCase(text)) {
            if (!UNDOING_NAMES.contains(text.toLowerCase())) {
                workout.getExercises().getLast().setComment(text);
                stepIndex++;
            } else {
                stepIndex--;
            }
        } else {
            stepIndex++;
        }
        return Optional.empty();
    }

    public Optional<Object> closeOrAddOneMoreExercise(String text) {
        String response;
        if ("REP".equalsIgnoreCase(text)) {
            stepIndex = 2;
            response = "Simbora!\n"+getMessage();
        } else if ("EX".equalsIgnoreCase(text)) {
            stepIndex = 1;
            response = "Simbora!\n"+getMessage();
        } else if (Constants.SUCCESSFUL_NAMES.contains(text.toLowerCase())) {
            response = SUCCESS;
            try {
                repository.save(workout);
            } catch (Exception e) {
                response = "Xiiiii...";
                logger.error("Erro: "+e.getMessage());
            }
        } else {
            response = getMessage();
        }
        return Optional.of(response);
    }

    private static String getOnlyNumbersFromText(String text) {
        text = text.trim();
        Pattern decimalPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher decimalMatcher = decimalPattern.matcher(text);
        StringBuilder decimalNumber = new StringBuilder();
        while (decimalMatcher.find()) {
            decimalNumber.append(decimalMatcher.group());
        }
        StringBuilder integerNumber = new StringBuilder();
        if (decimalNumber.isEmpty()) {
            Pattern integerPattern = Pattern.compile("-?\\d+");
            Matcher integerMatcher = integerPattern.matcher(text);
            while (integerMatcher.find()) {
                integerNumber.append(integerMatcher.group());
            }
        }
        return decimalNumber.isEmpty() ? integerNumber.toString() : decimalNumber.toString();
    }

    private void incrementExerciseIndexMessage() {
        this.steps.get(1).setMessage(
                this.steps.get(1).getMessage().replace(String.valueOf(exerciseIndex), String.valueOf(++exerciseIndex))
        );
    }
}