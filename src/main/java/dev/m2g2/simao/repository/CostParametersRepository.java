package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.catalog.CostParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostParametersRepository extends JpaRepository<CostParameters, Long> {

    // Single global row.
    Optional<CostParameters> findFirstByOrderByIdAsc();
}
