package dev.m2g2.simao.dto.management;

import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.PrintOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido como o admin o vê. `productionCost` e `profit` só são preenchidos para
 * o ADMIN: o OPERATOR precisa do preço de venda para falar com o cliente, mas
 * não do custo nem de quanto a loja ganha.
 */
public record OrderResponse(
        Long id,
        String description,
        CustomerResponse customer,
        Integer printTimeMinutes,
        Integer priority,
        OrderStatus status,
        String statusLabel,
        BigDecimal productionCost,
        BigDecimal salePrice,
        BigDecimal profit,
        LocalDateTime startedAt,
        String observations) {

    public static OrderResponse from(PrintOrder order, boolean withCosts) {
        BigDecimal cost = withCosts ? order.getProductionCost() : null;
        BigDecimal profit = null;
        if (withCosts && order.getProductionCost() != null && order.getSalePrice() != null)
            profit = order.getSalePrice().subtract(order.getProductionCost());

        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                CustomerResponse.from(order.getCustomer()),
                order.getPrintTimeMinutes(),
                order.getPriority(),
                order.getStatus(),
                order.getStatus().getLabel(),
                cost,
                order.getSalePrice(),
                profit,
                order.getStartedAt(),
                order.getObservations());
    }
}
