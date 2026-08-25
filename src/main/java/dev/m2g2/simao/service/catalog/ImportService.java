package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostParametersDTO;
import dev.m2g2.simao.dto.catalog.ImportProduct;
import dev.m2g2.simao.dto.catalog.ImportRequest;
import dev.m2g2.simao.dto.catalog.ImportResult;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.model.catalog.ProductPhoto;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports an argilalab.json project. Idempotent upsert: categories keyed by
 * code, products by SKU (category+num+tam). Running it twice updates rather
 * than duplicates. Nothing is deleted.
 */
@Service
public class ImportService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CostParametersService costParametersService;
    private final StoreConfigService storeConfigService;
    private final MediaService mediaService;

    public ImportService(CategoryRepository categoryRepository,
                         ProductRepository productRepository,
                         CostParametersService costParametersService,
                         StoreConfigService storeConfigService,
                         MediaService mediaService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.costParametersService = costParametersService;
        this.storeConfigService = storeConfigService;
        this.mediaService = mediaService;
    }

    // Media keys resolved during this import: key/data URI -> /api/media/<hash>.
    private final ThreadLocal<Map<String, String>> resolvedMedia =
            ThreadLocal.withInitial(LinkedHashMap::new);

    @Transactional
    public ImportResult importProject(ImportRequest request) {
        if (request == null || request.products() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payload inválido: esperado um argilalab.json com 'produtos'.");
        }

        resolvedMedia.get().clear();
        try {
            updateParamsIfComplete(request.params());
            if (request.store() != null) {
                storeConfigService.update(request.store());
            }
            int categories = upsertCategories(request.categories());
            storeMedia(request.media());

            int created = 0;
            int updated = 0;
            for (ImportProduct ip : request.products()) {
                boolean isNew = upsertProduct(ip);
                if (isNew) {
                    created++;
                } else {
                    updated++;
                }
            }
            return new ImportResult(categories, created, updated, resolvedMedia.get().size());
        } finally {
            resolvedMedia.remove();
        }
    }

    /**
     * Uploads the export's inline images once, mapping each media key to its
     * /api/media/<hash> URL so product photos can reference it.
     */
    private void storeMedia(Map<String, String> media) {
        if (media == null) {
            return;
        }
        for (Map.Entry<String, String> e : media.entrySet()) {
            String value = e.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            if (value.startsWith("data:")) {
                resolvedMedia.get().put(e.getKey(), mediaService.store(value).url());
            } else {
                // Already a URL (e.g. previously published to object storage).
                resolvedMedia.get().put(e.getKey(), value);
            }
        }
    }

    /**
     * A photo entry can be an external URL, an inline data: URI, or a key into
     * the export's `media` map. Everything ends up as a URL the frontend can use.
     */
    private List<String> resolvePhotos(ImportProduct ip) {
        List<String> raw = new ArrayList<>();
        if (ip.photos() != null) {
            raw.addAll(ip.photos());
        }
        if (raw.isEmpty() && ip.photo() != null) {
            raw.add(ip.photo());
        }
        List<String> urls = new ArrayList<>();
        for (String f : raw) {
            if (f == null || f.isBlank()) {
                continue;
            }
            String t = f.trim();
            // The app writes refs as "midia:<key>" while the export's `midia` map
            // is keyed by the bare key, so strip the prefix before looking it up.
            // The prefix stays Portuguese: it belongs to the external file format.
            boolean isRef = t.startsWith("midia:");
            String key = isRef ? t.substring(6) : t;
            String resolved;
            if (resolvedMedia.get().containsKey(key)) {
                resolved = resolvedMedia.get().get(key);
            } else if (key.startsWith("data:")) {
                resolved = mediaService.store(key).url();
                resolvedMedia.get().put(key, resolved);
            } else if (isRef) {
                // Dangling ref: no entry in `media`. Skip it instead of storing
                // "midia:<key>" as if it were a URL (renders as a broken image).
                continue;
            } else {
                resolved = t;
            }
            if (!urls.contains(resolved)) {
                urls.add(resolved);
            }
        }
        return urls;
    }

    private void updateParamsIfComplete(CostParametersDTO params) {
        if (params == null) {
            return;
        }
        boolean complete = params.filamentPricePerKg() != null && params.powerKw() != null
                && params.energyRate() != null && params.depreciationPerHour() != null && params.laborPerHour() != null
                && params.surchargePct() != null && params.markup() != null && params.marketplaceCommissionPct() != null
                && params.fixedFee() != null;
        if (complete) {
            costParametersService.update(params);
        }
    }

    private int upsertCategories(Map<String, String> categories) {
        if (categories == null) {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, String> e : categories.entrySet()) {
            String code = e.getKey() == null ? "" : e.getKey().trim().toUpperCase();
            String name = e.getValue();
            if (code.isEmpty() || name == null || name.isBlank()) {
                continue;
            }
            Category category = categoryRepository.findByCodeAndActiveTrue(code).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (category == null) {
                category = new Category();
                category.setCode(code);
                category.setCreatedAt(now);
                category.setActive(true);
            }
            category.setName(name.trim());
            category.setUpdatedAt(now);
            categoryRepository.save(category);
            count++;
        }
        return count;
    }

    private boolean upsertProduct(ImportProduct ip) {
        String code = ip.categoryCode() == null ? "" : ip.categoryCode().trim().toUpperCase();
        Category category = categoryRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Produto com categoria desconhecida: " + ip.categoryCode()));
        String name = ip.name() == null ? "" : ip.name().trim();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem nome no import.");
        }
        String size = ip.size() == null ? "" : ip.size().trim().toUpperCase();
        int num = ip.num() != null ? ip.num() : productRepository.findMaxNumByCategory(category.getId()) + 1;

        Product product = productRepository
                .findByCategoryIdAndNumAndSize(category.getId(), num, size)
                .orElse(null);
        boolean isNew = product == null;
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            product = new Product();
            product.setCreatedAt(now);
        }
        product.setCategory(category);
        product.setNum(num);
        product.setSize(size);
        product.setName(name);
        product.setDescription(trimOrNull(ip.description()));
        product.setStatus(ip.status() == null ? ProductStatus.ACTIVE : ip.status());
        product.setObservations(trimOrNull(ip.observations()));
        product.setGrams(ip.grams());
        product.setPrintTimeHours(ip.printTimeHours());
        product.setLaborMinutes(ip.laborMinutes());
        product.setSupplies(ip.supplies());
        product.setPackaging(ip.packaging());
        product.setCatalogPrice(ip.catalogPrice());
        product.setExactTime(ip.exactTime() == null ? true : ip.exactTime());
        product.setOrigin(trimOrNull(ip.origin()));
        product.setPrinter(trimOrNull(ip.printer()));
        product.setFilament(trimOrNull(ip.filament()));

        // v4 storefront fields, defaulting the same way the frontend migrar() does.
        product.setSlug(trimOrNull(ip.slug()));
        product.setLeadTimeDays(ip.leadTimeDays() == null ? 5 : ip.leadTimeDays());
        product.setSortOrder(ip.sortOrder() == null ? num * 10 : ip.sortOrder());
        product.setMaterial(ip.material() == null || ip.material().isBlank()
                ? "PLA rígido" : ip.material().trim());
        product.setPartDimensions(trimOrNull(ip.partDimensions()));
        product.setPackageWeight(ip.packageWeight());
        product.setPackageDimensions(trimOrNull(ip.packageDimensions()));
        product.setPublished(ip.published() == null
                ? product.getStatus() == ProductStatus.ACTIVE : ip.published());
        product.setFeatured(Boolean.TRUE.equals(ip.featured()));
        product.setLongDescription(trimOrNull(ip.longDescription()));
        product.setMetaDescription(trimOrNull(ip.metaDescription()));
        product.setLicense(trimOrNull(ip.license()));

        applyPhotos(product, resolvePhotos(ip), now);
        product.setActive(true);
        product.setUpdatedAt(now);
        productRepository.save(product);
        return isNew;
    }

    private void applyPhotos(Product product, List<String> urls, LocalDateTime now) {
        product.getPhotos().clear();
        int position = 0;
        for (String url : urls) {
            ProductPhoto photo = new ProductPhoto();
            photo.setProduct(product);
            photo.setUrl(url);
            photo.setPosition(position++);
            photo.setCreatedAt(now);
            photo.setUpdatedAt(now);
            photo.setActive(true);
            product.getPhotos().add(photo);
        }
        product.setPhoto(urls.isEmpty() ? null : urls.getFirst());
        product.setSlug(uniqueSlug(product));
    }

    /**
     * Slug has a unique index, so a collision inside the import would abort the
     * whole batch; resolve it here the same way the product service does.
     */
    private String uniqueSlug(Product product) {
        String base = product.getSlug() == null || product.getSlug().isBlank()
                ? ProductService.slugify(product.getName())
                : ProductService.slugify(product.getSlug());
        if (base.isEmpty()) {
            base = "produto";
        }
        String candidate = base;
        int suffix = 2;
        while (true) {
            Product owner = productRepository.findBySlugAndActiveTrue(candidate).orElse(null);
            if (owner == null || (product.getId() != null && owner.getId().equals(product.getId()))) {
                return candidate;
            }
            candidate = base + "-" + suffix++;
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
