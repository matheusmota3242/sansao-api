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
        if (request == null || request.produtos() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payload inválido: esperado um argilalab.json com 'produtos'.");
        }

        resolvedMedia.get().clear();
        try {
            updateParamsIfComplete(request.params());
            if (request.loja() != null) {
                storeConfigService.update(request.loja());
            }
            int categories = upsertCategories(request.cats());
            storeMedia(request.midia());

            int created = 0;
            int updated = 0;
            for (ImportProduct ip : request.produtos()) {
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
    private void storeMedia(Map<String, String> midia) {
        if (midia == null) {
            return;
        }
        for (Map.Entry<String, String> e : midia.entrySet()) {
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
     * the export's `midia` map. Everything ends up as a URL the frontend can use.
     */
    private List<String> resolvePhotos(ImportProduct ip) {
        List<String> raw = new ArrayList<>();
        if (ip.fotos() != null) {
            raw.addAll(ip.fotos());
        }
        if (raw.isEmpty() && ip.foto() != null) {
            raw.add(ip.foto());
        }
        List<String> urls = new ArrayList<>();
        for (String f : raw) {
            if (f == null || f.isBlank()) {
                continue;
            }
            String t = f.trim();
            String resolved;
            if (resolvedMedia.get().containsKey(t)) {
                resolved = resolvedMedia.get().get(t);
            } else if (t.startsWith("data:")) {
                resolved = mediaService.store(t).url();
                resolvedMedia.get().put(t, resolved);
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
        boolean complete = params.filPreco() != null && params.potencia() != null
                && params.tarifa() != null && params.deprec() != null && params.mdo() != null
                && params.acresc() != null && params.markup() != null && params.comissao() != null
                && params.taxaFixa() != null;
        if (complete) {
            costParametersService.update(params);
        }
    }

    private int upsertCategories(Map<String, String> cats) {
        if (cats == null) {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, String> e : cats.entrySet()) {
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
        String code = ip.cat() == null ? "" : ip.cat().trim().toUpperCase();
        Category category = categoryRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Produto com categoria desconhecida: " + ip.cat()));
        String nome = ip.nome() == null ? "" : ip.nome().trim();
        if (nome.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem nome no import.");
        }
        String tam = ip.tam() == null ? "" : ip.tam().trim().toUpperCase();
        int num = ip.num() != null ? ip.num() : productRepository.findMaxNumByCategory(category.getId()) + 1;

        Product product = productRepository
                .findByCategoryIdAndNumAndTam(category.getId(), num, tam)
                .orElse(null);
        boolean isNew = product == null;
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            product = new Product();
            product.setCreatedAt(now);
        }
        product.setCategory(category);
        product.setNum(num);
        product.setTam(tam);
        product.setNome(nome);
        product.setDescricao(trimOrNull(ip.desc()));
        product.setStatus(ip.status() == null ? ProductStatus.ATIVO : ip.status());
        product.setObs(trimOrNull(ip.obs()));
        product.setGram(ip.gram());
        product.setTempoHoras(ip.tempo());
        product.setTrabMin(ip.trab());
        product.setInsumos(ip.ins());
        product.setEmbalagem(ip.emb());
        product.setCatalogoPreco(ip.catalogo());
        product.setTempoExato(ip.tempoExato() == null ? true : ip.tempoExato());
        product.setOrigem(trimOrNull(ip.origem()));
        product.setImpressora(trimOrNull(ip.impressora()));
        product.setFilamento(trimOrNull(ip.filamento()));

        // v4 storefront fields, defaulting the same way the frontend migrar() does.
        product.setSlug(trimOrNull(ip.slug()));
        product.setPrazo(ip.prazo() == null ? 5 : ip.prazo());
        product.setOrdem(ip.ordem() == null ? num * 10 : ip.ordem());
        product.setMaterial(ip.material() == null || ip.material().isBlank()
                ? "PLA rígido" : ip.material().trim());
        product.setDimPeca(trimOrNull(ip.dimPeca()));
        product.setEmbPeso(ip.embPeso());
        product.setEmbDim(trimOrNull(ip.embDim()));
        product.setPublicado(ip.publicado() == null
                ? product.getStatus() == ProductStatus.ATIVO : ip.publicado());
        product.setDestaque(Boolean.TRUE.equals(ip.destaque()));
        product.setDescLonga(trimOrNull(ip.descLonga()));
        product.setMetaDesc(trimOrNull(ip.metaDesc()));
        product.setLicenca(trimOrNull(ip.licenca()));

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
        product.setFoto(urls.isEmpty() ? null : urls.getFirst());
        product.setSlug(uniqueSlug(product));
    }

    /**
     * Slug has a unique index, so a collision inside the import would abort the
     * whole batch; resolve it here the same way the product service does.
     */
    private String uniqueSlug(Product product) {
        String base = product.getSlug() == null || product.getSlug().isBlank()
                ? ProductService.slugify(product.getNome())
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
