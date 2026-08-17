package dev.m2g2.simao.util;

import java.math.BigDecimal;

public class OrderInputUtil {

    /**
     * Parses print time in minutes, also accepting the HHhMM / HH:MM shorthands
     * (4h30, 4:30) since that is how print times are usually quoted. Throws
     * IllegalArgumentException when the value is not a positive duration.
     */
    public static Integer parsePrintTimeMinutes(String value) {
        String normalized = value.trim().toLowerCase().replace(" ", "");
        if (normalized.isEmpty())
            throw new IllegalArgumentException("Print time must be informed");

        String separator = normalized.contains("h") ? "h" : (normalized.contains(":") ? ":" : null);
        int minutes;
        if (separator != null) {
            String[] parts = normalized.split(separator, -1);
            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid print time");
            int hours = parseNonNegative(parts[0], 0);
            // "4h" means four hours flat.
            int remainder = parseNonNegative(parts[1], 0);
            if (remainder >= 60)
                throw new IllegalArgumentException("Minutes must be below 60");
            minutes = hours * 60 + remainder;
        } else {
            minutes = parseNonNegative(normalized, -1);
        }

        if (minutes <= 0)
            throw new IllegalArgumentException("Print time must be positive");
        return minutes;
    }

    private static int parseNonNegative(String value, int fallbackWhenBlank) {
        if (value.isBlank()) {
            if (fallbackWhenBlank < 0)
                throw new IllegalArgumentException("Invalid number");
            return fallbackWhenBlank;
        }
        int parsed = Integer.parseInt(value);
        if (parsed < 0)
            throw new IllegalArgumentException("Value must not be negative");
        return parsed;
    }

    /**
     * Monetary parsing shares the purchase rules (Brazilian or dot decimals,
     * optional R$ prefix), so it delegates instead of duplicating them.
     */
    public static BigDecimal parsePrice(String value) {
        return PurchaseInputUtil.parsePrice(value);
    }

    /**
     * Renders minutes as the compact form used in the queue listing: 90 -> 1h30,
     * 45 -> 45min, 120 -> 2h.
     */
    public static String formatMinutes(Integer minutes) {
        if (minutes == null)
            return "—";
        int hours = minutes / 60;
        int remainder = minutes % 60;
        if (hours == 0)
            return "%dmin".formatted(remainder);
        if (remainder == 0)
            return "%dh".formatted(hours);
        return "%dh%02d".formatted(hours, remainder);
    }
}
