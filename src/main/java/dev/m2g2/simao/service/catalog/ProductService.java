package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostBreakdown;
import dev.m2g2.simao.dto.catalog.ProductRequest;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import dev.m2g2.simao.util.SkuUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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
        Category category = resolveCategory(request.cat());
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
        Category category = resolveCategory(request.cat());
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
        copy.setNome(original.getNome());
        copy.setDescricao(original.getDescricao());
        copy.setStatus(original.getStatus());
        copy.setObs(original.getObs());
        copy.setGram(original.getGram());
        copy.setTempoHoras(original.getTempoHoras());
        copy.setTrabMin(original.getTrabMin());
        copy.setInsumos(original.getInsumos());
        copy.setEmbalagem(original.getEmbalagem());
        copy.setCatalogoPreco(original.getCatalogoPreco());
        copy.setTempoExato(original.isTempoExato());
        copy.setFoto(original.getFoto());
        copy.setOrigem(original.getOrigem());
        copy.setImpressora(original.getImpressora());
        copy.setFilamento(original.getFilamento());
        copy.setTam("");
        copy.setNum(nextNum(original.getCategory()));
        LocalDateTime now = LocalDateTime.now();
        copy.setCreatedAt(now);
        copy.setUpdatedAt(now);
        copy.setActive(true);
        return toResponse(save(copy), costParametersService.getEntity());
    }

    // ---- helpers ------------------------------------------------------------

    private void applyRequest(Product product, ProductRequest request, Category category) {
        String nome = request.nome() == null ? "" : request.nome().trim();
        if (nome.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dê um nome ao produto.");
        }
        product.setCategory(category);
        product.setNome(nome);
        product.setTam(request.tam() == null ? "" : request.tam().trim().toUpperCase());
        product.setStatus(request.status() == null ? ProductStatus.ATIVO : request.status());
        product.setObs(trimOrNull(request.obs()));
        product.setDescricao(trimOrNull(request.desc()));
        product.setFoto(trimOrNull(request.foto()));
        product.setOrigem(trimOrNull(request.origem()));
        product.setImpressora(trimOrNull(request.impressora()));
        product.setFilamento(trimOrNull(request.filamento()));
        product.setGram(request.gram());
        product.setTempoHoras(request.tempo());
        product.setTrabMin(request.trab());
        product.setInsumos(request.ins());
        product.setEmbalagem(request.emb());
        product.setCatalogoPreco(request.catalogo());
        product.setTempoExato(true);
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
                    "SKU já existe: " + SkuUtil.build(product.getCategory().getCode(), product.getNum(), product.getTam()));
        }
    }

    private boolean matchesQuery(ProductResponse r, String query) {
        String haystack = (r.nome() + " " + r.sku() + " " + (r.desc() == null ? "" : r.desc())).toLowerCase();
        return haystack.contains(query);
    }

    private Comparator<ProductResponse> comparator(String sort) {
        String s = sort == null ? "sku" : sort;
        return switch (s) {
            case "nome" -> Comparator.comparing(ProductResponse::nome, String.CASE_INSENSITIVE_ORDER);
            case "preco" -> Comparator.comparing(
                    (ProductResponse r) -> r.catalogo() == null ? BigDecimal.valueOf(-1) : r.catalogo()).reversed();
            case "margem" -> Comparator.comparing(
                    (ProductResponse r) -> r.custo().margemPct() == null
                            ? BigDecimal.valueOf(-900) : r.custo().margemPct()).reversed();
            default -> Comparator.comparing(ProductResponse::sku);
        };
    }

    private ProductResponse toResponse(Product p, CostParameters params) {
        CostBreakdown custo = costCalculatorService.compute(p, params);
        Category c = p.getCategory();
        return new ProductResponse(
                p.getId(),
                SkuUtil.build(c.getCode(), p.getNum(), p.getTam()),
                c.getCode(),
                c.getName(),
                p.getNum(),
                p.getTam(),
                p.getNome(),
                p.getDescricao(),
                p.getStatus(),
                p.getObs(),
                p.getGram(),
                p.getTempoHoras(),
                p.getTrabMin(),
                p.getInsumos(),
                p.getEmbalagem(),
                p.getCatalogoPreco(),
                p.isTempoExato(),
                p.getFoto(),
                p.getOrigem(),
                p.getImpressora(),
                p.getFilamento(),
                custo);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
