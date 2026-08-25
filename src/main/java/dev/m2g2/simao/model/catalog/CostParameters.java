package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Global cost parameters — a single row. Changing it re-derives every product's
 * computed cost (nothing derived is persisted on the product).
 */
@Entity
public class CostParameters extends BaseModel {

    @Column(name = "filament_price_per_kg")
    private BigDecimal filamentPricePerKg;      // R$/kg
    private BigDecimal powerKw;      // kW
    private BigDecimal energyRate;        // R$/kWh
    private BigDecimal depreciationPerHour;        // R$/h
    private BigDecimal laborPerHour;           // R$/h
    private BigDecimal surchargePct;        // %
    private BigDecimal markup;        // markup x
    private BigDecimal marketplaceCommissionPct;      // % do marketplace
    @Column(name = "fixed_fee")
    private BigDecimal fixedFee;      // R$

    public BigDecimal getFilamentPricePerKg() {
        return filamentPricePerKg;
    }

    public void setFilamentPricePerKg(BigDecimal filamentPricePerKg) {
        this.filamentPricePerKg = filamentPricePerKg;
    }

    public BigDecimal getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(BigDecimal powerKw) {
        this.powerKw = powerKw;
    }

    public BigDecimal getEnergyRate() {
        return energyRate;
    }

    public void setEnergyRate(BigDecimal energyRate) {
        this.energyRate = energyRate;
    }

    public BigDecimal getDepreciationPerHour() {
        return depreciationPerHour;
    }

    public void setDepreciationPerHour(BigDecimal depreciationPerHour) {
        this.depreciationPerHour = depreciationPerHour;
    }

    public BigDecimal getLaborPerHour() {
        return laborPerHour;
    }

    public void setLaborPerHour(BigDecimal laborPerHour) {
        this.laborPerHour = laborPerHour;
    }

    public BigDecimal getSurchargePct() {
        return surchargePct;
    }

    public void setSurchargePct(BigDecimal surchargePct) {
        this.surchargePct = surchargePct;
    }

    public BigDecimal getMarkup() {
        return markup;
    }

    public void setMarkup(BigDecimal markup) {
        this.markup = markup;
    }

    public BigDecimal getMarketplaceCommissionPct() {
        return marketplaceCommissionPct;
    }

    public void setMarketplaceCommissionPct(BigDecimal marketplaceCommissionPct) {
        this.marketplaceCommissionPct = marketplaceCommissionPct;
    }

    public BigDecimal getFixedFee() {
        return fixedFee;
    }

    public void setFixedFee(BigDecimal fixedFee) {
        this.fixedFee = fixedFee;
    }
}
