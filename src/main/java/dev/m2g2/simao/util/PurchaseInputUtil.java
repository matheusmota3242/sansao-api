package dev.m2g2.simao.util;

import java.math.BigDecimal;

public class PurchaseInputUtil {

    /**
     * Parses a whole-number quantity. Throws IllegalArgumentException when the
     * value is not a positive integer.
     */
    public static Integer parseAmount(String value) {
        int amount = Integer.parseInt(value.trim());
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        return amount;
    }

    /**
     * Parses a monetary value, accepting both Brazilian (12,50) and dot (12.50)
     * decimal separators and an optional leading R$. Throws
     * IllegalArgumentException when the value is not a positive amount.
     */
    public static BigDecimal parsePrice(String value) {
        String normalized = value.trim().replace("R$", "").replace(" ", "");
        // When a comma is present it is the decimal separator (Brazilian format),
        // so dots are thousands separators. Otherwise the dot is the decimal
        // separator (12.50) and must be preserved.
        if (normalized.contains(",")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        }
        BigDecimal price = new BigDecimal(normalized);
        if (price.signum() <= 0)
            throw new IllegalArgumentException("Price must be positive");
        return price;
    }
}
