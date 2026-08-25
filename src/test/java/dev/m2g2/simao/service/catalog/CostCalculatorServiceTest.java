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
        p.setFilamentPricePerKg(new BigDecimal("89.00"));
        p.setPowerKw(new BigDecimal("0.150"));
        p.setEnergyRate(new BigDecimal("0.75"));
        p.setDepreciationPerHour(new BigDecimal("0.58"));
        p.setLaborPerHour(new BigDecimal("10.00"));
        p.setSurchargePct(new BigDecimal("10"));
        p.setMarkup(new BigDecimal("2.0"));
        p.setMarketplaceCommissionPct(new BigDecimal("25.5"));
        p.setFixedFee(new BigDecimal("4.00"));
        return p;
    }

    private Product product(String grams, String printTimeHours, String laborMinutes, String supplies, String packaging, String catalogPrice) {
        Product p = new Product();
        p.setGrams(grams == null ? null : new BigDecimal(grams));
        p.setPrintTimeHours(printTimeHours == null ? null : new BigDecimal(printTimeHours));
        p.setLaborMinutes(laborMinutes == null ? null : new BigDecimal(laborMinutes));
        p.setSupplies(supplies == null ? null : new BigDecimal(supplies));
        p.setPackaging(packaging == null ? null : new BigDecimal(packaging));
        p.setCatalogPrice(catalogPrice == null ? null : new BigDecimal(catalogPrice));
        return p;
    }

    @Test
    void compute_matchesFrontendCalcForKnownProduct() {
        // "Rolo de Esferas — 25 mm": gram 53, tempo 1.6667h, trab 10min, emb 1.00, catalogo 39.00
        Product p = product("53", "1.6667", "10", "0", "1.00", "39.00");

        CostBreakdown c = service.compute(p, defaultParams());

        assertEquals(new BigDecimal("4.72"), c.filament());
        assertEquals(new BigDecimal("0.19"), c.energy());
        assertEquals(new BigDecimal("0.97"), c.depreciation());
        assertEquals(new BigDecimal("1.67"), c.labor());
        assertEquals(new BigDecimal("8.54"), c.subtotal());
        assertEquals(new BigDecimal("9.39"), c.finalCost());
        assertEquals(new BigDecimal("18.78"), c.suggestedPrice());
        assertEquals(new BigDecimal("29.21"), c.marketplacePrice());
        assertEquals(new BigDecimal("39.00"), c.catalogPrice());
        assertEquals(new BigDecimal("29.61"), c.margin());
        assertEquals(new BigDecimal("75.9"), c.marginPct());
    }

    @Test
    void compute_noCatalogPrice_leavesMarginNull() {
        Product p = product("100", "2", "10", "0", "0", null);

        CostBreakdown c = service.compute(p, defaultParams());

        assertNull(c.catalogPrice());
        assertNull(c.margin());
        assertNull(c.marginPct());
        assertNotNull(c.finalCost());
        assertNotNull(c.suggestedPrice());
    }

    @Test
    void compute_nullNumericFieldsTreatedAsZero() {
        Product p = product(null, null, null, null, null, null);

        CostBreakdown c = service.compute(p, defaultParams());

        assertEquals(new BigDecimal("0.00"), c.subtotal());
        assertEquals(new BigDecimal("0.00"), c.finalCost());
    }
}
