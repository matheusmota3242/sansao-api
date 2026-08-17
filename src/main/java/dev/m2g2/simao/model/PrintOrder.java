package dev.m2g2.simao.model;

import dev.m2g2.simao.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A customer order, which doubles as an entry in the printing queue.
 * Mapped to print_order because ORDER is a reserved SQL keyword.
 */
@Entity
@Table(name = "print_order")
public class PrintOrder extends BaseModel {

    private String description;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "print_time_minutes")
    private Integer printTimeMinutes;

    /**
     * 1-based position in the queue, contiguous across orders whose status is
     * still WAITING or RUNNING. Null once the order leaves the queue.
     */
    private Integer priority;

    // STRING rather than ORDINAL so inserting a value into the middle of the
    // enum later cannot silently reinterpret rows already persisted.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.WAITING;

    @Column(name = "production_cost")
    private BigDecimal productionCost;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    private String observations;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getPrintTimeMinutes() {
        return printTimeMinutes;
    }

    public void setPrintTimeMinutes(Integer printTimeMinutes) {
        this.printTimeMinutes = printTimeMinutes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
