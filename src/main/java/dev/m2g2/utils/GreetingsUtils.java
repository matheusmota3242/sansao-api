package dev.m2g2.utils;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GreetingsUtils {

    private GreetingsUtils() {
        throw new RuntimeException("Utility class");
    }

    private static Random random = new Random();

    private static final String EXCLAMATION = "!";
    private static final String INTERROGATION = "?";

    private static final Map<String, String> greetings = Map.of(
            "Fala", EXCLAMATION,
            "Fala comigo", EXCLAMATION,
            "E aí", EXCLAMATION,
            "Habla", EXCLAMATION,
            "Buenos", EXCLAMATION,
            "Buenos dias", EXCLAMATION,
            "Bom dia", EXCLAMATION,
            "Olá", EXCLAMATION,
            "Na paz", INTERROGATION,
            "Beleza", INTERROGATION);

    private static final List<String> congrats = List.of(
            "Boa",
            "Aí sim",
            "Sabe muito",
            "Conhece",
            "Se garantiu",
            "Se garante",
            "Mostrou para que veio",
            "Parabéns",
            "Wow",
            "Simbora",
            "Simba",
            "Chama",
            "Aiin"
    );

    private static final List<String> nicknames = List.of(
            "meu querido",
            "jogabonito",
            "fera",
            "galado",
            "irmão",
            "hermano",
            "filósofo",
            "herói",
            "guerreiro",
            "vencedor",
            "gigante",
            "champs"
    );

    public static String getRandomicNickname() {
        return nicknames.get(random.nextInt(nicknames.size()));
    }

    public static String getRandomicCongratulations() {
        return congrats.get(random.nextInt(congrats.size()))+", "+nicknames.get(random.nextInt(nicknames.size()))+"!";
    }

    public static String getRandomicGreetings() {
        String greeting = greetings.keySet().stream().toList().get(random.nextInt(greetings.size()));
        String pontuation = greetings.get(greeting);
        String nickname = nicknames.get(random.nextInt(nicknames.size()));
        return greeting+", "+nickname+pontuation;
    }

}
