package dev.m2g2.simao.dto.management;

import dev.m2g2.simao.enums.Role;
import dev.m2g2.simao.model.User;

/**
 * Quem está logado. O frontend usa isto para não desenhar o que a pessoa não
 * pode ver — a resposta da API já omite o dado, isto só evita campo vazio.
 */
public record MeResponse(String name, String email, Role role, boolean canSeeCosts) {

    public static MeResponse from(User user) {
        return new MeResponse(user.getName(), user.getEmail(), user.getRole(),
                user.getRole().canSeeCosts());
    }
}
