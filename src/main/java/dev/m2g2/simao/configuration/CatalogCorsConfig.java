package dev.m2g2.simao.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Abre CORS apenas no feed público, e apenas para as origens da loja.
 *
 * O admin é servido pelo próprio jar (mesma origem), então não precisa de CORS
 * nenhum — e liberá-lo seria perigoso: com sessão por cookie, origem liberada
 * mais credenciais é o que permite a um site qualquer agir em seu nome.
 */
@Configuration
public class CatalogCorsConfig implements WebMvcConfigurer {

    private final String[] storefrontOrigins;

    public CatalogCorsConfig(@Value("${application.storefront-origins:}") String origins) {
        this.storefrontOrigins = origins.isBlank() ? new String[0] : origins.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (storefrontOrigins.length == 0)
            return;
        registry.addMapping("/api/catalog")
                .allowedOrigins(storefrontOrigins)
                .allowedMethods("GET");
        registry.addMapping("/api/media/*")
                .allowedOrigins(storefrontOrigins)
                .allowedMethods("GET");
    }
}
