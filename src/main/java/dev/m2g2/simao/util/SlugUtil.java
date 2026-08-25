package dev.m2g2.simao.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Slug para URL. Espelha o slugify() do frontend: tira acento, baixa a caixa,
 * troca o resto por hífen e corta em 70 — para que um produto tenha a mesma URL
 * venha o catálogo do servidor ou do navegador.
 */
public final class SlugUtil {

    private SlugUtil() {
    }

    public static String of(String text) {
        if (text == null)
            return "";
        String semAcento = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.length() > 70 ? slug.substring(0, 70) : slug;
    }
}
