package dev.m2g2.simao.security;

import dev.m2g2.simao.model.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Diz ao Spring Data quem está gravando, preenchendo created_by/updated_by.
 * Devolve vazio quando não há ninguém autenticado — é o caso da semeadura do
 * primeiro admin, que por definição não tem criador.
 */
@Component
public class AuditorProvider implements AuditorAware<User> {

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return Optional.empty();
        if (auth.getPrincipal() instanceof AppUserDetails details)
            return Optional.of(details.getUser());
        return Optional.empty();
    }
}
