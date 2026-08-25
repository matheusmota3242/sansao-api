package dev.m2g2.simao.security;

import dev.m2g2.simao.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public AppUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCaseAndActiveTrue(email)
                .map(AppUserDetails::new)
                // Mensagem genérica de propósito: não revela se o e-mail existe.
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
    }
}
