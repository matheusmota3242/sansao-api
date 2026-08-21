package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.catalog.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByHash(String hash);
}
