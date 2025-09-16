package dev.m2g2.utils;

import java.util.Set;

public class Constants {
    private Constants() {
        throw new RuntimeException("Utility class");
    }
    public static final String SUCCESS = "SUCCESS";
    public static final Set<String> CANCELLING_NAMES = Set.of("cancel", "cancelar", "canc");
    public static final Set<String> SUCCESSFUL_NAMES = Set.of("show", "fechou", "pronto", "noiz", "nois", "nozes");
    public static final Set<String> UNDOING_NAMES = Set.of("undo", "desfazer");
}