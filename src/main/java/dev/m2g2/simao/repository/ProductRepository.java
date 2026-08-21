package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.catalog.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByActiveTrue();

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    Optional<Product> findBySlugAndActiveTrue(String slug);

    // Upsert key for import: a product is identified by its SKU (category+num+tam).
    Optional<Product> findByCategoryIdAndNumAndTam(Long categoryId, Integer num, String tam);

    // Max num over ALL rows in the category (inactive included), so a soft-deleted
    // SKU stays reserved and never gets reused.
    @Query("SELECT COALESCE(MAX(p.num), 0) FROM Product p WHERE p.category.id = :categoryId")
    int findMaxNumByCategory(@Param("categoryId") Long categoryId);
}
