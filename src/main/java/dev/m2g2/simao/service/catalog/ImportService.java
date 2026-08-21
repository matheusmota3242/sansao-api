package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostParametersDTO;
import dev.m2g2.simao.dto.catalog.ImportProduct;
import dev.m2g2.simao.dto.catalog.ImportRequest;
import dev.m2g2.simao.dto.catalog.ImportResult;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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

    public ImportService(CategoryRepository categoryRepository,
                         ProductRepository productRepository,
                         CostParametersService costParametersService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.costParametersService = costParametersService;
    }

    @Transactional
    public ImportResult importProject(ImportRequest request) {
        if (request == null || request.produtos() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payload inválido: esperado um argilalab.json com 'produtos'.");
        }

        updateParamsIfComplete(request.params());
        int categories = upsertCategories(request.cats());

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
        return new ImportResult(categories, created, updated);
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
        product.setFoto(trimOrNull(ip.foto()));
        product.setOrigem(trimOrNull(ip.origem()));
        product.setImpressora(trimOrNull(ip.impressora()));
        product.setFilamento(trimOrNull(ip.filamento()));
        product.setActive(true);
        product.setUpdatedAt(now);
        productRepository.save(product);
        return isNew;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
