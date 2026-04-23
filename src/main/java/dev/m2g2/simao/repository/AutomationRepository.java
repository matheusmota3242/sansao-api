package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.automation.Automation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AutomationRepository extends JpaRepository<Automation, Long> {

    List<Automation> findAllByActiveTrue();

    @Query(value = """
            SELECT * FROM automation a\s
            JOIN automation_schedule as ON as.automation_id = a.id\s
            WHERE a.active\s
            """, nativeQuery = true)
    List<Automation> findAllWithSchedule();

    List<Automation> findAllByActiveTrueAndNextExecutionAtBefore(LocalDateTime now, Pageable pageable);

}