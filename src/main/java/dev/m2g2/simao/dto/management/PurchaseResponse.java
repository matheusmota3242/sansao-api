package dev.m2g2.simao.dto.management;

import dev.m2g2.simao.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        String description,
        Integer amount,
        BigDecimal unitPrice,
        BigDecimal total,
        String source,
        LocalDate purchasedAt,
        String observations,
        LocalDateTime createdAt) {

    public static PurchaseResponse from(Purchase purchase) {
        BigDecimal total = purchase.getUnitPrice() == null || purchase.getAmount() == null
                ? null
                : purchase.getUnitPrice().multiply(BigDecimal.valueOf(purchase.getAmount()));
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getAmount(),
                purchase.getUnitPrice(),
                total,
                purchase.getSource(),
                purchase.getPurchasedAt(),
                purchase.getObservations(),
                purchase.getCreatedAt());
    }
}
