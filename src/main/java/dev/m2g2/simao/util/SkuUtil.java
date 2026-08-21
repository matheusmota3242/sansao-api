package dev.m2g2.simao.util;

/**
 * Derives the ARGILA LAB SKU: AL-&lt;categoryCode&gt;-&lt;num:3&gt;[-&lt;tam&gt;].
 */
public final class SkuUtil {

    private SkuUtil() {
    }

    public static String build(String categoryCode, Integer num, String tam) {
        String base = "AL-" + categoryCode + "-" + String.format("%03d", num == null ? 0 : num);
        return (tam == null || tam.isBlank()) ? base : base + "-" + tam;
    }
}
