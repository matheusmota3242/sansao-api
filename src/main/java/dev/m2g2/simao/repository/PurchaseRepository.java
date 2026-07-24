package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAllByActiveTrueOrderByCreatedAtDesc();
}
