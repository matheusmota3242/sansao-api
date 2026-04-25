package dev.m2g2.simao.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DateTimeUtil {
    private static Map<Integer, String> daysOfWeek = Map.of(0, "DOM", 1, "SEG", 2, "TER", 3, "QUA", 4, "QUI", 5, "SEX", 6, "SAB");

    public static Set<String> getDaysOfWeekAbreviations() {
        return new HashSet<>(daysOfWeek.values());
    }

    public static String getDayOfWeekAbreviation(int dayOfWeek) {
        return daysOfWeek.get(dayOfWeek);
    }

    public static int getDayOfWeekIndex(String dayOfWeek) {
        return daysOfWeek.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(dayOfWeek))
                .findFirst()
                .orElseThrow()
                .getKey();
    }

    public static String getFormattedDaysOfWeekAbreviations() {
        return daysOfWeek.values().stream().sorted().collect(Collectors.joining(", "));
    }
}
