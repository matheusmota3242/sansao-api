package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CatalogResponse;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.dto.catalog.PublicCategory;
import dev.m2g2.simao.dto.catalog.PublicProduct;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.util.SlugUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo público da loja: só os produtos publicados, na ordem da vitrine.
 *
 * Projeta cada produto para o {@link PublicProduct}, que é estreito de
 * propósito: este endpoint não exige login, então custo, margem, gramatura,
 * tempo de impressão, impressora, filamento e observações internas não podem
 * sair daqui.
 */
@Service
public class CatalogService {

    private static final String BRAND = "argila lab";

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
        Map<String, String> categoryNames = new LinkedHashMap<>();
        for (Category c : categoryRepository.findAllByActiveTrueOrderByCodeAsc())
            categoryNames.put(c.getCode(), c.getName());

        List<String> categoryOrder = new ArrayList<>(categoryNames.keySet());

        List<ProductResponse> published = productService.list(null, null, null, "sku").stream()
                .filter(ProductResponse::published)
                // Vitrine: agrupa por categoria, dentro dela manda o campo ordem,
                // e o desempate é o SKU.
                .sorted(Comparator
                        .comparingInt((ProductResponse p) -> {
                            int i = categoryOrder.indexOf(p.categoryCode());
                            return i < 0 ? Integer.MAX_VALUE : i;
                        })
                        .thenComparingInt(p -> p.sortOrder() == null ? 0 : p.sortOrder())
                        .thenComparing(ProductResponse::sku))
                .toList();

        List<PublicCategory> categories = categoryNames.entrySet().stream()
                .filter(e -> published.stream().anyMatch(p -> e.getKey().equals(p.categoryCode())))
                .map(e -> new PublicCategory(e.getKey(), e.getValue(), SlugUtil.of(e.getValue())))
                .toList();

        List<PublicProduct> products = published.stream().map(this::toPublic).toList();

        return new CatalogResponse(BRAND, storeConfigService.get(), categories, products,
                LocalDateTime.now());
    }

    private PublicProduct toPublic(ProductResponse p) {
        List<String> photos = p.photos() != null && !p.photos().isEmpty()
                ? p.photos()
                : (p.photo() != null && !p.photo().isBlank() ? List.of(p.photo()) : List.of());

        return new PublicProduct(
                p.sku(),
                p.slug() != null && !p.slug().isBlank() ? p.slug() : SlugUtil.of(p.name()),
                p.name(),
                p.categoryName(),
                p.categoryCode(),
                p.description() == null ? "" : p.description(),
                p.longDescription() != null && !p.longDescription().isBlank()
                        ? p.longDescription()
                        : (p.description() == null ? "" : p.description()),
                p.metaDescription() == null ? "" : p.metaDescription(),
                p.catalogPrice(),
                photos,
                p.material() == null ? "" : p.material(),
                p.partDimensions() == null ? "" : p.partDimensions(),
                p.leadTimeDays(),
                p.packageWeight(),
                p.packageDimensions() == null ? "" : p.packageDimensions(),
                p.featured(),
                p.sortOrder() == null ? 0 : p.sortOrder());
    }
}
