package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.ImportProduct;
import dev.m2g2.simao.dto.catalog.ImportRequest;
import dev.m2g2.simao.dto.catalog.ImportResult;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.Product;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CostParametersService costParametersService;

    private ImportService service;
    private Category mol;

    @BeforeEach
    void setUp() {
        service = new ImportService(categoryRepository, productRepository, costParametersService);
        mol = new Category();
        mol.setId(1L);
        mol.setCode("MOL");
        mol.setName("Moldes e Formas");
        lenient().when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ImportProduct product(int num, String tam, String nome) {
        return new ImportProduct("MOL", num, tam, nome, "desc", null,
                new BigDecimal("50"), new BigDecimal("2"), new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("39.00"),
                true, null, null, null, null, null);
    }

    @Test
    void import_createsMissingProductAndUpsertsCategory() {
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.empty(), Optional.of(mol));
        when(productRepository.findByCategoryIdAndNumAndTam(1L, 1, "")).thenReturn(Optional.empty());

        ImportRequest req = new ImportRequest(null, Map.of("MOL", "Moldes e Formas"),
                List.of(product(1, "", "Molde novo")));

        ImportResult result = service.importProject(req);

        assertEquals(1, result.categories());
        assertEquals(1, result.productsCreated());
        assertEquals(0, result.productsUpdated());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void import_updatesExistingProductBySku() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setCategory(mol);
        existing.setNum(1);
        existing.setTam("");
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.of(mol));
        when(productRepository.findByCategoryIdAndNumAndTam(1L, 1, "")).thenReturn(Optional.of(existing));

        ImportRequest req = new ImportRequest(null, null, List.of(product(1, "", "Nome atualizado")));

        ImportResult result = service.importProject(req);

        assertEquals(0, result.productsCreated());
        assertEquals(1, result.productsUpdated());
        assertEquals("Nome atualizado", existing.getNome());
        verify(productRepository).save(existing);
    }

    @Test
    void import_missingProdutos_returns400() {
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.importProject(new ImportRequest(null, null, null)));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void import_unknownCategoryOnProduct_returns400() {
        when(categoryRepository.findByCodeAndActiveTrue("MOL")).thenReturn(Optional.empty());

        ImportRequest req = new ImportRequest(null, null, List.of(product(1, "", "Item")));

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.importProject(req));
        assertEquals(400, ex.getStatusCode().value());
    }
}
