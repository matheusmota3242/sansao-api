package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostBreakdown;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.model.catalog.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CostCalculatorServiceTest {

    private final CostCalculatorService service = new CostCalculatorService();

    // App default parameters.
    private CostParameters defaultParams() {
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

    private Product product(String gram, String tempo, String trab, String ins, String emb, String catalogo) {
        Product p = new Product();
        p.setGram(gram == null ? null : new BigDecimal(gram));
        p.setTempoHoras(tempo == null ? null : new BigDecimal(tempo));
        p.setTrabMin(trab == null ? null : new BigDecimal(trab));
        p.setInsumos(ins == null ? null : new BigDecimal(ins));
        p.setEmbalagem(emb == null ? null : new BigDecimal(emb));
        p.setCatalogoPreco(catalogo == null ? null : new BigDecimal(catalogo));
        return p;
    }

    @Test
    void compute_matchesFrontendCalcForKnownProduct() {
        // "Rolo de Esferas — 25 mm": gram 53, tempo 1.6667h, trab 10min, emb 1.00, catalogo 39.00
        Product p = product("53", "1.6667", "10", "0", "1.00", "39.00");

        CostBreakdown c = service.compute(p, defaultParams());

        assertEquals(new BigDecimal("4.72"), c.filamento());
        assertEquals(new BigDecimal("0.19"), c.energia());
        assertEquals(new BigDecimal("0.97"), c.depreciacao());
        assertEquals(new BigDecimal("1.67"), c.maoDeObra());
        assertEquals(new BigDecimal("8.54"), c.subtotal());
        assertEquals(new BigDecimal("9.39"), c.custoFinal());
        assertEquals(new BigDecimal("18.78"), c.precoSugerido());
        assertEquals(new BigDecimal("29.21"), c.precoMarketplace());
        assertEquals(new BigDecimal("39.00"), c.catalogo());
        assertEquals(new BigDecimal("29.61"), c.margem());
        assertEquals(new BigDecimal("75.9"), c.margemPct());
    }

    @Test
    void compute_noCatalogPrice_leavesMarginNull() {
        Product p = product("100", "2", "10", "0", "0", null);

        CostBreakdown c = service.compute(p, defaultParams());

        assertNull(c.catalogo());
        assertNull(c.margem());
        assertNull(c.margemPct());
        assertNotNull(c.custoFinal());
        assertNotNull(c.precoSugerido());
    }

    @Test
    void compute_nullNumericFieldsTreatedAsZero() {
        Product p = product(null, null, null, null, null, null);

        CostBreakdown c = service.compute(p, defaultParams());

        assertEquals(new BigDecimal("0.00"), c.subtotal());
        assertEquals(new BigDecimal("0.00"), c.custoFinal());
    }
}
