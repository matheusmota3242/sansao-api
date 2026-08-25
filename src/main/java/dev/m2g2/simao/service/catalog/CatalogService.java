package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CatalogResponse;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public catalog for the storefront: only published products, ordered the way
 * the store shows them. Replaces the catalogo.json the app used to publish.
 */
@Service
public class CatalogService {

    private final ProductService productService;
    private final StoreConfigService storeConfigService;
    private final CategoryRepository categoryRepository;

    public CatalogService(ProductService productService,
                          StoreConfigService storeConfigService,
                          CategoryRepository categoryRepository) {
        this.productService = productService;
        this.storeConfigService = storeConfigService;
        this.categoryRepository = categoryRepository;
    }

    public CatalogResponse get() {
        List<ProductResponse> published = productService.list(null, null, null, "sku").stream()
                .filter(ProductResponse::published)
                .sorted(Comparator
                        .comparing((ProductResponse p) -> p.sortOrder() == null ? Integer.MAX_VALUE : p.sortOrder())
                        .thenComparing(ProductResponse::sku))
                .toList();

        Map<String, String> categories = new LinkedHashMap<>();
        for (Category c : categoryRepository.findAllByActiveTrueOrderByCodeAsc()) {
            categories.put(c.getCode(), c.getName());
        }

        return new CatalogResponse(storeConfigService.get(), categories, published, LocalDateTime.now());
    }
}
