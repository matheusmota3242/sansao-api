package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostBreakdown;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.model.catalog.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes a product's cost/pricing from the global cost parameters, mirroring
 * the frontend calc(). Nothing here is persisted — cost is always derived, so
 * changing a parameter reflects on every product immediately.
 */
@Service
public class CostCalculatorService {

    public CostBreakdown compute(Product p, CostParameters params) {
        double filamentPricePerKg = d(params.getFilamentPricePerKg());
        double powerKw = d(params.getPowerKw());
        double energyRate = d(params.getEnergyRate());
        double depreciationPerHour = d(params.getDepreciationPerHour());
        double laborPerHour = d(params.getLaborPerHour());
        double surchargePct = d(params.getSurchargePct());
        double markup = d(params.getMarkup());
        double marketplaceCommissionPct = d(params.getMarketplaceCommissionPct());
        double fixedFee = d(params.getFixedFee());

        double grams = d(p.getGrams());
        double printTimeHours = d(p.getPrintTimeHours());
        double laborMinutes = d(p.getLaborMinutes());
        double supplies = d(p.getSupplies());
        double packaging = d(p.getPackaging());

        double filam = grams * filamentPricePerKg / 1000.0;
        double energ = printTimeHours * powerKw * energyRate;
        double depr = printTimeHours * depreciationPerHour;
        double mao = laborMinutes / 60.0 * laborPerHour;
        double sub = supplies + packaging + filam + energ + depr + mao;
        double fin = sub * (1 + surchargePct / 100.0);
        double sug = fin * markup;
        double denom = 1 - marketplaceCommissionPct / 100.0;
        Double mkt = denom == 0 ? null : sug / denom + fixedFee;

        BigDecimal catalogPrice = p.getCatalogPrice();
        BigDecimal margin = null;
        BigDecimal marginPct = null;
        if (catalogPrice != null) {
            double categoryCode = catalogPrice.doubleValue();
            double mg = categoryCode - fin;
            margin = money(mg);
            if (categoryCode != 0.0) {
                marginPct = pct(mg / categoryCode * 100.0);
            }
        }

        return new CostBreakdown(
                money(filam), money(energ), money(depr), money(mao),
                money(supplies), money(packaging), money(sub), money(fin),
                money(sug), mkt == null ? null : money(mkt),
                catalogPrice == null ? null : catalogPrice.setScale(2, RoundingMode.HALF_UP),
                margin, marginPct);
    }

    private static double d(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }

    private static BigDecimal money(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal pct(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP);
    }
}
