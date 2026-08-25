package dev.m2g2.simao.dto.management;

import dev.m2g2.simao.model.Customer;

public record CustomerResponse(Long id, String name) {

    public static CustomerResponse from(Customer customer) {
        return customer == null ? null : new CustomerResponse(customer.getId(), customer.getName());
    }
}
