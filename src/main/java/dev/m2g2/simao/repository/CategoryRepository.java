package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<Category> findAllByActiveTrueOrderByCodeAsc();
}
