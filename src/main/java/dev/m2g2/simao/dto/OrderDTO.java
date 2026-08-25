package dev.m2g2.simao.dto;

import java.math.BigDecimal;

public class OrderDTO {

    private String description;
    /**
     * Accepts either a customer id or a name; the service resolves it to an
     * existing Customer or creates one. See CustomerService#resolveByNameOrId.
     */
    private String customerName;
    private Integer printTimeMinutes;
    private BigDecimal productionCost;
    private BigDecimal salePrice;
    private String observations;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getPrintTimeMinutes() {
        return printTimeMinutes;
    }

    public void setPrintTimeMinutes(Integer printTimeMinutes) {
        this.printTimeMinutes = printTimeMinutes;
    }

    public BigDecimal getProductionCost() {
        return productionCost;
    }

    public void setProductionCost(BigDecimal productionCost) {
        this.productionCost = productionCost;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
