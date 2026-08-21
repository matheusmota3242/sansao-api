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
        double filPreco = d(params.getFilPreco());
        double potencia = d(params.getPotencia());
        double tarifa = d(params.getTarifa());
        double deprec = d(params.getDeprec());
        double mdo = d(params.getMdo());
        double acresc = d(params.getAcresc());
        double markup = d(params.getMarkup());
        double comissao = d(params.getComissao());
        double taxaFixa = d(params.getTaxaFixa());

        double gram = d(p.getGram());
        double tempo = d(p.getTempoHoras());
        double trab = d(p.getTrabMin());
        double ins = d(p.getInsumos());
        double emb = d(p.getEmbalagem());

        double filam = gram * filPreco / 1000.0;
        double energ = tempo * potencia * tarifa;
        double depr = tempo * deprec;
        double mao = trab / 60.0 * mdo;
        double sub = ins + emb + filam + energ + depr + mao;
        double fin = sub * (1 + acresc / 100.0);
        double sug = fin * markup;
        double denom = 1 - comissao / 100.0;
        Double mkt = denom == 0 ? null : sug / denom + taxaFixa;

        BigDecimal catalogo = p.getCatalogoPreco();
        BigDecimal margem = null;
        BigDecimal margemPct = null;
        if (catalogo != null) {
            double cat = catalogo.doubleValue();
            double mg = cat - fin;
            margem = money(mg);
            if (cat != 0.0) {
                margemPct = pct(mg / cat * 100.0);
            }
        }

        return new CostBreakdown(
                money(filam), money(energ), money(depr), money(mao),
                money(ins), money(emb), money(sub), money(fin),
                money(sug), mkt == null ? null : money(mkt),
                catalogo == null ? null : catalogo.setScale(2, RoundingMode.HALF_UP),
                margem, margemPct);
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
