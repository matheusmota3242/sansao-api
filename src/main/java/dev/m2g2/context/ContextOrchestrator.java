package dev.m2g2.context;

import dev.m2g2.repository.WorkoutRepository;
import dev.m2g2.utils.GreetingsUtils;

import java.util.Optional;

import static dev.m2g2.utils.Constants.CANCELLING_NAMES;
import static dev.m2g2.utils.Constants.SUCCESS;
import static dev.m2g2.utils.GreetingsUtils.getRandomicCongratulations;

public class ContextOrchestrator {

    private AbstractContext context;
    private final WorkoutRepository repository;

    private static final String MENU = """
                                    Diz o que quer:
    
                                    [ 1  ] Novo treino
                                    [ 2 ] Listar todos treinos
                                    [ 3 ] Consultar treinos por palavra-chave
                                    """;

    public ContextOrchestrator(WorkoutRepository repository) {
        this.repository = repository;
    }


    public String getResponseMessage(String textReceived) {
        String response;
        if (context != null) {
            response = getResponseFromExistentContext(textReceived);
        } else {
            response = getResponseFromNewContext(textReceived);
        }
        return response;
    }

    private String getResponseFromNewContext(String textReceived) {
        String response;
        switch (textReceived) {
            case "1" -> {
                context = new NewWorkoutContext(repository);
                response = context.getMessage();
            }
            case "2" -> {
                context = new GetWorkoutContext(repository);
                response = context.getMessage();
                context = null;
            }
            case "3" -> {
                context = new GetWorkoutByStringContext(repository);
                response = context.getMessage();
            }
            default -> response = GreetingsUtils.getRandomicGreetings()+"\n"+MENU;
        }
        return response;
    }

    private String getResponseFromExistentContext(String textReceived) {
        String response = null;
        if (CANCELLING_NAMES.contains(textReceived.toLowerCase())) {
            context = null;
            response = MENU;
        } else {
            Optional<Object> optionalOutput = context.processInput(textReceived);
            if (optionalOutput.isPresent()) {
                if (optionalOutput.get() instanceof AbstractContext) {
                    context = (AbstractContext) optionalOutput.get();
                    response = context.getMessage();
                } else if (optionalOutput.get().equals(SUCCESS)) {
                    context = null;
                    response = getRandomicCongratulations();
                } else if (optionalOutput.get() instanceof String) {
                    response = (String) optionalOutput.get();
                }
            } else {
                response = context.getMessage();
            }
        }
        return response;
    }
}
