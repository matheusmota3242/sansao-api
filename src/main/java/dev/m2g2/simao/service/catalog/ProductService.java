package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostBreakdown;
import dev.m2g2.simao.dto.catalog.ProductRequest;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.model.catalog.ProductPhoto;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import dev.m2g2.simao.util.SkuUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final CostParametersService costParametersService;
    private final CostCalculatorService costCalculatorService;

    public ProductService(ProductRepository repository,
                          CategoryRepository categoryRepository,
                          CostParametersService costParametersService,
                          CostCalculatorService costCalculatorService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.costParametersService = costParametersService;
        this.costCalculatorService = costCalculatorService;
    }

    public List<ProductResponse> list(String q, String catCode, ProductStatus status, String sort) {
        CostParameters params = costParametersService.getEntity();
        String query = q == null ? "" : q.trim().toLowerCase();

        List<ProductResponse> result = repository.findAllByActiveTrue().stream()
                .filter(p -> catCode == null || catCode.isBlank()
                        || catCode.equalsIgnoreCase(p.getCategory().getCode()))
                .filter(p -> status == null || status == p.getStatus())
                .map(p -> toResponse(p, params))
                .filter(r -> query.isEmpty() || matchesQuery(r, query))
                .sorted(comparator(sort))
                .toList();
        return result;
    }

    public ProductResponse getById(Long id) {
        CostParameters params = costParametersService.getEntity();
        return toResponse(findActive(id), params);
    }

    public ProductResponse create(ProductRequest request) {
        Category category = resolveCategory(request.categoryCode());
        Product product = new Product();
        applyRequest(product, request, category);
        product.setNum(nextNum(category));
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        product.setActive(true);
        return toResponse(save(product), costParametersService.getEntity());
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findActive(id);
        Category category = resolveCategory(request.categoryCode());
        boolean categoryChanged = !product.getCategory().getId().equals(category.getId());
        applyRequest(product, request, category);
        if (categoryChanged) {
            product.setNum(nextNum(category));
        }
        product.setUpdatedAt(LocalDateTime.now());
        return toResponse(save(product), costParametersService.getEntity());
    }

    public void delete(Long id) {
        Product product = findActive(id);
        // Soft delete keeps the SKU number reserved (nextNum spans inactive rows too).
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        repository.save(product);
    }

    public ProductResponse duplicate(Long id) {
        Product original = findActive(id);
        Product copy = new Product();
        copy.setCategory(original.getCategory());
        copy.setName(original.getName());
        copy.setDescription(original.getDescription());
        copy.setStatus(original.getStatus());
        copy.setObservations(original.getObservations());
        copy.setGrams(original.getGrams());
        copy.setPrintTimeHours(original.getPrintTimeHours());
        copy.setLaborMinutes(original.getLaborMinutes());
        copy.setSupplies(original.getSupplies());
        copy.setPackaging(original.getPackaging());
        copy.setCatalogPrice(original.getCatalogPrice());
        copy.setExactTime(original.isExactTime());
        copy.setOrigin(original.getOrigin());
        copy.setPrinter(original.getPrinter());
        copy.setFilament(original.getFilament());
        copy.setLeadTimeDays(original.getLeadTimeDays());
        copy.setSortOrder(original.getSortOrder());
        copy.setMaterial(original.getMaterial());
        copy.setPartDimensions(original.getPartDimensions());
        copy.setPackageWeight(original.getPackageWeight());
        copy.setPackageDimensions(original.getPackageDimensions());
        // A copy starts unpublished: it still needs a name/price review.
        copy.setPublished(false);
        copy.setFeatured(false);
        copy.setLongDescription(original.getLongDescription());
        copy.setMetaDescription(original.getMetaDescription());
        copy.setLicense(original.getLicense());
        copy.setSize("");
        copy.setNum(nextNum(original.getCategory()));
        // Same images, new rows; slug must differ from the original's.
        applyPhotos(copy, original.getPhotos().stream().map(ProductPhoto::getUrl).toList());
        copy.setSlug(resolveSlug(null, original.getName(), copy));
        LocalDateTime now = LocalDateTime.now();
        copy.setCreatedAt(now);
        copy.setUpdatedAt(now);
        copy.setActive(true);
        return toResponse(save(copy), costParametersService.getEntity());
    }

    // ---- helpers ------------------------------------------------------------

    private void applyRequest(Product product, ProductRequest request, Category category) {
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dê um nome ao produto.");
        }
        product.setCategory(category);
        product.setName(name);
        product.setSize(request.size() == null ? "" : request.size().trim().toUpperCase());
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : request.status());
        product.setObservations(trimOrNull(request.observations()));
        product.setDescription(trimOrNull(request.description()));
        product.setOrigin(trimOrNull(request.origin()));
        product.setPrinter(trimOrNull(request.printer()));
        product.setFilament(trimOrNull(request.filament()));
        product.setGrams(request.grams());
        product.setPrintTimeHours(request.printTimeHours());
        product.setLaborMinutes(request.laborMinutes());
        product.setSupplies(request.supplies());
        product.setPackaging(request.packaging());
        product.setCatalogPrice(request.catalogPrice());
        product.setExactTime(true);

        // --- storefront / SEO ---
        product.setSlug(resolveSlug(request.slug(), name, product));
        product.setLeadTimeDays(request.leadTimeDays() == null ? 5 : request.leadTimeDays());
        product.setSortOrder(request.sortOrder());
        product.setMaterial(request.material() == null || request.material().isBlank()
                ? "PLA rígido" : request.material().trim());
        product.setPartDimensions(trimOrNull(request.partDimensions()));
        product.setPackageWeight(request.packageWeight());
        product.setPackageDimensions(trimOrNull(request.packageDimensions()));
        // Default: publish what is active, mirroring the frontend migrar().
        product.setPublished(request.published() == null
                ? product.getStatus() == ProductStatus.ACTIVE : request.published());
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setLongDescription(trimOrNull(request.longDescription()));
        product.setMetaDescription(trimOrNull(request.metaDescription()));
        product.setLicense(trimOrNull(request.license()));

        applyPhotos(product, request.photos());
    }

    /**
     * Replaces the photo list in order. photos[0] is mirrored onto `foto` so the
     * cover stays available to anything reading the flat field.
     */
    private void applyPhotos(Product product, List<String> photos) {
        List<String> urls = new ArrayList<>();
        if (photos != null) {
            for (String f : photos) {
                String t = f == null ? "" : f.trim();
                if (!t.isEmpty() && !urls.contains(t)) {
                    urls.add(t);
                }
            }
        }
        product.getPhotos().clear();
        int position = 0;
        LocalDateTime now = LocalDateTime.now();
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
    }

    /** Slug is the storefront URL and must be unique across products. */
    private String resolveSlug(String requested, String name, Product product) {
        String base = (requested == null || requested.isBlank()) ? slugify(name) : slugify(requested);
        if (base.isEmpty()) {
            base = "produto";
        }
        String candidate = base;
        int suffix = 2;
        while (true) {
            Product owner = repository.findBySlugAndActiveTrue(candidate).orElse(null);
            if (owner == null || owner.getId().equals(product.getId())) {
                return candidate;
            }
            candidate = base + "-" + suffix++;
        }
    }

    static String slugify(String value) {
        if (value == null) {
            return "";
        }
        String n = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return n;
    }

    private Category resolveCategory(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria é obrigatória.");
        }
        return categoryRepository.findByCodeAndActiveTrue(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Categoria %s não encontrada.".formatted(code)));
    }

    private int nextNum(Category category) {
        return repository.findMaxNumByCategory(category.getId()) + 1;
    }

    private Product findActive(Long id) {
        return repository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    private Product save(Product product) {
        try {
            return repository.save(product);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "SKU já existe: " + SkuUtil.build(product.getCategory().getCode(), product.getNum(), product.getSize()));
        }
    }

    private boolean matchesQuery(ProductResponse r, String query) {
        String haystack = (r.name() + " " + r.sku() + " " + (r.description() == null ? "" : r.description())).toLowerCase();
        return haystack.contains(query);
    }

    private Comparator<ProductResponse> comparator(String sort) {
        String s = sort == null ? "sku" : sort;
        return switch (s) {
            case "nome" -> Comparator.comparing(ProductResponse::name, String.CASE_INSENSITIVE_ORDER);
            case "preco" -> Comparator.comparing(
                    (ProductResponse r) -> r.catalogPrice() == null ? BigDecimal.valueOf(-1) : r.catalogPrice()).reversed();
            case "margem" -> Comparator.comparing(
                    (ProductResponse r) -> r.cost().marginPct() == null
                            ? BigDecimal.valueOf(-900) : r.cost().marginPct()).reversed();
            default -> Comparator.comparing(ProductResponse::sku);
        };
    }

    private ProductResponse toResponse(Product p, CostParameters params) {
        CostBreakdown cost = costCalculatorService.compute(p, params);
        Category c = p.getCategory();
        List<String> photos = p.getPhotos().stream().map(ProductPhoto::getUrl).toList();
        return new ProductResponse(
                p.getId(),
                SkuUtil.build(c.getCode(), p.getNum(), p.getSize()),
                c.getCode(),
                c.getName(),
                p.getNum(),
                p.getSize(),
                p.getName(),
                p.getDescription(),
                p.getStatus(),
                p.getObservations(),
                p.getGrams(),
                p.getPrintTimeHours(),
                p.getLaborMinutes(),
                p.getSupplies(),
                p.getPackaging(),
                p.getCatalogPrice(),
                p.isExactTime(),
                p.getPhoto(),
                photos,
                p.getOrigin(),
                p.getPrinter(),
                p.getFilament(),
                p.getSlug(),
                p.getLeadTimeDays(),
                p.getSortOrder(),
                p.getMaterial(),
                p.getPartDimensions(),
                p.getPackageWeight(),
                p.getPackageDimensions(),
                p.isPublished(),
                p.isFeatured(),
                p.getLongDescription(),
                p.getMetaDescription(),
                p.getLicense(),
                cost);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
