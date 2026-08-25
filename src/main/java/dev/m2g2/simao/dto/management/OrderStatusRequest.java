package dev.m2g2.simao.dto.management;

import dev.m2g2.simao.enums.OrderStatus;

public record OrderStatusRequest(OrderStatus status) {
}
