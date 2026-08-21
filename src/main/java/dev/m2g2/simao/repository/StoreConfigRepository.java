package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.catalog.StoreConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreConfigRepository extends JpaRepository<StoreConfig, Long> {
    // Single global row.
    Optional<StoreConfig> findFirstByOrderByIdAsc();
}
