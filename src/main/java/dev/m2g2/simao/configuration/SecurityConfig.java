package dev.m2g2.simao.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.LinkedHashMap;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A API responde 401; as páginas HTML vão para o login. Um fetch() seguiria
     * um redirect e tentaria ler o HTML de login como JSON.
     *
     * Montado à mão de propósito: com um único mapeamento,
     * defaultAuthenticationEntryPointFor passa a valer para tudo e o matcher é
     * ignorado — o que mandava 401 até para quem só abriu o admin no navegador.
     */
    private AuthenticationEntryPoint entryPoint() {
        RequestMatcher isApi = request -> request.getRequestURI().startsWith("/api/");
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> mappings = new LinkedHashMap<>();
        mappings.put(isApi, new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        DelegatingAuthenticationEntryPoint delegating = new DelegatingAuthenticationEntryPoint(mappings);
        delegating.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login.html"));
        return delegating;
    }

    /**
     * Falha de CSRF é tratada antes do entry point, e sem isto uma escrita sem
     * token voltava 302 para o HTML de login — que um fetch() seguiria e
     * tentaria ler como JSON. Na API, negado é status, não página.
     */
    private AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandlerImpl paraPaginas = new AccessDeniedHandlerImpl();
        return (request, response, denied) -> {
            if (request.getRequestURI().startsWith("/api/"))
                response.sendError(HttpStatus.FORBIDDEN.value(), "Acesso negado.");
            else
                paraPaginas.handle(request, response, denied);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // O token de CSRF vai num cookie legível por JavaScript para que o
        // admin possa devolvê-lo no header X-XSRF-TOKEN. Sem isso, sessão por
        // cookie deixaria a API aberta a requisições forjadas de outro site.
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                    .csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(csrfHandler))
            .authorizeHttpRequests(auth -> auth
                    // A loja pública: só leitura, e é o que o storefront estático consome.
                    .requestMatchers(HttpMethod.GET, "/api/catalog", "/api/media/*").permitAll()
                    .requestMatchers("/login", "/login.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                    // Custos e margem são do ADMIN; o OPERATOR opera sem ver quanto se ganha.
                    .requestMatchers("/api/cost-parameters/**").hasRole("ADMIN")
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(entryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
            .formLogin(form -> form
                    .loginPage("/login.html")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/argilalabapp.html", true)
                    .failureUrl("/login.html?erro")
                    .permitAll())
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login.html?saiu")
                    .permitAll());

        return http.build();
    }
}
