package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.tracker.Tracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackerRepository extends JpaRepository<Tracker, Long> {

    Optional<Tracker> findByKeywordAndActiveTrue(String keyword);

    boolean existsByKeyword(String keyword);

    List<Tracker> findAllByActiveTrueOrderByCreatedAtDesc();
}
