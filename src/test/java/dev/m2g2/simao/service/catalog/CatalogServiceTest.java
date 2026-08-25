package dev.m2g2.simao.service.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.m2g2.simao.dto.catalog.CatalogResponse;
import dev.m2g2.simao.dto.catalog.CostBreakdown;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.dto.catalog.StoreConfigDTO;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductService productService;
    @Mock
    private StoreConfigService storeConfigService;
    @Mock
    private CategoryRepository categoryRepository;

    private CatalogService service;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        service = new CatalogService(productService, storeConfigService, categoryRepository);
        Category mol = new Category();
        mol.setCode("MOL");
        mol.setName("Moldes e Formas");
        when(categoryRepository.findAllByActiveTrueOrderByCodeAsc()).thenReturn(List.of(mol));
        when(storeConfigService.get()).thenReturn(new StoreConfigDTO(
                "argila_lab", "5584", new BigDecimal("250"), "hero", "texto",
                List.of(), List.of(), List.of(), "rodapé", "obs"));
    }

    private ProductResponse produto(boolean publicado) {
        return new ProductResponse(
                1L, "AL-MOL-001", "MOL", "Moldes e Formas", 1, "", "Molde de tigela",
                "resumo", ProductStatus.ACTIVE, "observação interna",
                new BigDecimal("140"), new BigDecimal("6"), new BigDecimal("20"),
                new BigDecimal("4"), new BigDecimal("3"), new BigDecimal("189.00"),
                true, null, List.of("/api/media/abc"), "origem interna",
                "Bambu X1", "PLA da marca tal",
                "molde-de-tigela", 7, 10, "PLA rígido", "240x240", new BigDecimal("400"),
                "30x30x10", publicado, true, "descrição longa", "meta", "CC",
                new CostBreakdown(
                        new BigDecimal("12.3"), new BigDecimal("1.1"), new BigDecimal("3.5"),
                        new BigDecimal("3.3"), new BigDecimal("4"), new BigDecimal("3"),
                        new BigDecimal("27.2"), new BigDecimal("29.9"), new BigDecimal("59.8"),
                        new BigDecimal("90"), new BigDecimal("189"), new BigDecimal("159.1"),
                        new BigDecimal("84.2")));
    }

    @Test
    void publicCatalogNeverCarriesCostOrInternalFields() throws Exception {
        when(productService.list(any(), any(), any(), any())).thenReturn(List.of(produto(true)));

        JsonNode raiz = mapper.valueToTree(service.get());
        JsonNode produto = raiz.get("produtos").get(0);

        // Confere por nome de campo, e não por substring: "gram" casaria dentro
        // de outra palavra e o teste passaria a mentir nas duas direções.
        Set<String> campos = new HashSet<>();
        produto.fieldNames().forEachRemaining(campos::add);

        for (String proibido : List.of("custo", "gram", "tempo", "trab", "ins", "emb",
                "impressora", "filamento", "origem", "obs", "tempoExato", "status",
                "publicado", "num", "id", "catalogo")) {
            assertFalse(campos.contains(proibido), "vazou no catálogo público: " + proibido);
        }

        // Nenhum valor interno pode aparecer, esteja em que campo estiver.
        String json = mapper.writeValueAsString(raiz);
        for (String segredo : List.of("Bambu X1", "PLA da marca tal", "observação interna",
                "origem interna", "84.2", "29.9")) {
            assertFalse(json.contains(segredo), "vazou no catálogo público: " + segredo);
        }

        // E o que a vitrine precisa continua lá.
        assertEquals("Molde de tigela", produto.get("nome").asText());
        assertEquals("molde-de-tigela", produto.get("slug").asText());
        assertEquals("PLA rígido", produto.get("material").asText());
        assertEquals(7, produto.get("prazoDias").asInt());
        assertTrue(produto.get("preco").decimalValue().compareTo(new BigDecimal("189")) == 0);
        assertEquals(1, produto.get("fotos").size());
    }

    @Test
    void unpublishedProductsStayOut() {
        when(productService.list(any(), any(), any(), any())).thenReturn(List.of(produto(false)));

        CatalogResponse c = service.get();

        assertTrue(c.products().isEmpty());
        assertTrue(c.categories().isEmpty(), "categoria sem produto publicado não aparece");
    }

    @Test
    void categoriesCarryASlugForTheUrl() {
        when(productService.list(any(), any(), any(), any())).thenReturn(List.of(produto(true)));

        CatalogResponse c = service.get();

        assertEquals(1, c.categories().size());
        assertEquals("moldes-e-formas", c.categories().get(0).slug());
    }
}
