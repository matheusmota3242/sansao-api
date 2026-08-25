package dev.m2g2.simao.dto.management;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        String description,
        Integer amount,
        BigDecimal unitPrice,
        String source,
        LocalDate purchasedAt,
        String observations) {
}
