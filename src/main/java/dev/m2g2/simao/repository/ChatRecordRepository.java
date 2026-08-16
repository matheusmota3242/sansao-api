package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.chat.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {

    /**
     * Rows written before chatId existed (chat_id IS NULL) predate multi-chat
     * support entirely, so they were always the owner's — they must keep
     * resolving here.
     */
    @Query("SELECT c FROM ChatRecord c WHERE (c.chatId = :chatId OR c.chatId IS NULL) " +
           "AND c.participantId IS NULL AND c.completed = false AND c.active = true " +
           "ORDER BY c.id DESC LIMIT 1")
    Optional<ChatRecord> findLastPendingForOwnerChat(@Param("chatId") String chatId);

    Optional<ChatRecord> findFirstByChatIdAndParticipantIdAndCompletedFalseAndActiveTrueOrderByIdDesc(
            String chatId, String participantId);
}
