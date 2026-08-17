package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.tracker.TrackerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TrackerEntryRepository extends JpaRepository<TrackerEntry, Long> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM TrackerEntry e " +
           "WHERE e.tracker.id = :trackerId AND e.active = true " +
           "AND e.recordedAt >= :start AND e.recordedAt < :end")
    BigDecimal sumAmountForPeriod(@Param("trackerId") Long trackerId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
