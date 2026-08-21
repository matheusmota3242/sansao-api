package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.CatalogResponse;
import dev.m2g2.simao.service.catalog.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public feed consumed by the storefront (published products + store config). */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    public CatalogResponse get() {
        return service.get();
    }
}
