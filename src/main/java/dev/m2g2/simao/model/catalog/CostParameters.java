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

    @Column(name = "fil_preco")
    private BigDecimal filPreco;      // filamento R$/kg
    private BigDecimal potencia;      // kW
    private BigDecimal tarifa;        // R$/kWh
    private BigDecimal deprec;        // depreciacao R$/h
    private BigDecimal mdo;           // mao de obra R$/h
    private BigDecimal acresc;        // acrescimo %
    private BigDecimal markup;        // markup x
    private BigDecimal comissao;      // comissao marketplace %
    @Column(name = "taxa_fixa")
    private BigDecimal taxaFixa;      // taxa fixa R$

    public BigDecimal getFilPreco() {
        return filPreco;
    }

    public void setFilPreco(BigDecimal filPreco) {
        this.filPreco = filPreco;
    }

    public BigDecimal getPotencia() {
        return potencia;
    }

    public void setPotencia(BigDecimal potencia) {
        this.potencia = potencia;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    public BigDecimal getDeprec() {
        return deprec;
    }

    public void setDeprec(BigDecimal deprec) {
        this.deprec = deprec;
    }

    public BigDecimal getMdo() {
        return mdo;
    }

    public void setMdo(BigDecimal mdo) {
        this.mdo = mdo;
    }

    public BigDecimal getAcresc() {
        return acresc;
    }

    public void setAcresc(BigDecimal acresc) {
        this.acresc = acresc;
    }

    public BigDecimal getMarkup() {
        return markup;
    }

    public void setMarkup(BigDecimal markup) {
        this.markup = markup;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao;
    }

    public BigDecimal getTaxaFixa() {
        return taxaFixa;
    }

    public void setTaxaFixa(BigDecimal taxaFixa) {
        this.taxaFixa = taxaFixa;
    }
}
