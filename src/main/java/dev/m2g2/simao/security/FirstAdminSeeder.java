package dev.m2g2.simao.security;

import dev.m2g2.simao.enums.Role;
import dev.m2g2.simao.model.User;
import dev.m2g2.simao.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro ADMIN no boot, senão não haveria como entrar no admin de um
 * banco recém-criado. Só age quando a tabela está vazia: uma vez que exista
 * qualquer usuário, mudar as variáveis não mexe em ninguém — inclusive não
 * ressuscita a senha de um admin que você trocou depois.
 */
@Component
public class FirstAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirstAdminSeeder.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public FirstAdminSeeder(UserRepository repository,
                            PasswordEncoder passwordEncoder,
                            @Value("${application.first-admin.email:}") String email,
                            @Value("${application.first-admin.password:}") String password,
                            @Value("${application.first-admin.name:Administrador}") String name) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0)
            return;

        if (email.isBlank() || password.isBlank()) {
            log.warn("Nenhum usuário cadastrado e ADMIN_EMAIL/ADMIN_PASSWORD não foram "
                    + "definidos: ninguém consegue entrar no admin. Defina as duas e reinicie.");
            return;
        }

        User admin = new User();
        admin.setName(name);
        admin.setEmail(email.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        repository.save(admin);
        log.info("Primeiro admin criado: {}. Troque a senha assim que entrar.", admin.getEmail());
    }
}
