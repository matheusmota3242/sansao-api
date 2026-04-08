package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.chat.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {

    Optional<ChatRecord> findFirstByCompletedFalseAndActiveTrueOrderByIdDesc();
}
