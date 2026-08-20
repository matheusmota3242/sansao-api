package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.ProductRequest;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CostParametersService costParametersService;

    private ProductService service;

    private Category mol;

    @BeforeEach
    void setUp() {
        // Use the real calculator; only repositories/params are mocked.
        service = new ProductService(repository, categoryRepository,
                costParametersService, new CostCalculatorService());
        mol = new Category();
        mol.setId(1L);
        mol.setCode("MOL");
        mol.setName("Moldes e Formas");
        lenient().when(costParametersService.getEntity()).thenReturn(params());
        lenient().when(repository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            if (p.getId() == null) p.setId(99L);
            return p;
        });
    }

    private CostParameters params() {
        CostParameters p = new CostParameters();
        p.setFilPreco(new BigDecimal("89.00"));
        p.setPotencia(new BigDecimal("0.150"));
        p.setTarifa(new BigDecimal("0.75"));
        p.setDeprec(new BigDecimal("0.58"));
        p.setMdo(new BigDecimal("10.00"));
        p.setAcresc(new BigDecimal("10"));
        p.setMarkup(new BigDecimal("2.0"));
        p.setComissao(new BigDecimal("25.5"));
        p.setTaxaFixa(new BigDecimal("4.00"));
        return p;
    }

    private ProductRequest req(String nome, String cat, String tam) {
        return new ProductRequest(nome, cat, tam, ProductStatus.ATIVO, null, "desc",
                null, null, null, null,
                new BigDecimal("50"), new BigDecimal("2"), new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("39.00"));
    }

    @Test
    void create_assignsNextNumAndBuildsSku() {
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.of(mol));
        when(repository.findMaxNumByCategory(1L)).thenReturn(4);

        ProductResponse r = service.create(req("Molde novo", "MOL", ""));

        assertEquals(5, r.num());
        assertEquals("AL-MOL-005", r.sku());
        assertEquals("MOL", r.cat());
        assertEquals("Moldes e Formas", r.catNome());
        assertNotNull(r.custo());
        verify(repository).save(any(Product.class));
    }

    @Test
    void create_withTam_appendsSuffixAndUppercases() {
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.of(mol));
        when(repository.findMaxNumByCategory(1L)).thenReturn(0);

        ProductResponse r = service.create(req("Caneca", "MOL", "p"));

        assertEquals("AL-MOL-001-P", r.sku());
        assertEquals("P", r.tam());
    }

    @Test
    void create_blankName_returns400() {
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.of(mol));

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.create(req("  ", "MOL", "")));
        assertEquals(400, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void create_unknownCategory_returns400() {
        when(categoryRepository.findByCodeAndActiveTrue("XXX")).thenReturn(Optional.empty());

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.create(req("Item", "XXX", "")));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void duplicate_resetsTamAndAssignsNextNum() {
        Product original = new Product();
        original.setId(7L);
        original.setCategory(mol);
        original.setNome("Original");
        original.setTam("G");
        original.setActive(true);
        when(repository.findById(7L)).thenReturn(Optional.of(original));
        when(repository.findMaxNumByCategory(1L)).thenReturn(3);

        ProductResponse r = service.duplicate(7L);

        assertEquals("", r.tam());
        assertEquals(4, r.num());
        assertEquals("AL-MOL-004", r.sku());
        assertEquals("Original", r.nome());
    }

    @Test
    void delete_softDeletes() {
        Product p = new Product();
        p.setId(7L);
        p.setCategory(mol);
        p.setActive(true);
        when(repository.findById(7L)).thenReturn(Optional.of(p));

        service.delete(7L);

        assertFalse(p.isActive());
        verify(repository).save(p);
    }

    @Test
    void getById_missing_returns404() {
        when(repository.findById(50L)).thenReturn(Optional.empty());

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.getById(50L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
