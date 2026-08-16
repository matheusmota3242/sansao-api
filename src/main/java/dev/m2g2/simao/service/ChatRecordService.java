package dev.m2g2.simao.service;

import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.repository.ChatRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ChatRecordService {

    private final ChatRecordRepository repository;

    public ChatRecordService(ChatRecordRepository repository) {
        this.repository = repository;
    }

    public void create(ChatRecord chatRecord) {
        LocalDateTime now = LocalDateTime.now();
        chatRecord.setCreatedAt(now);
        chatRecord.setUpdatedAt(now);
        repository.save(chatRecord);
    }

    public Optional<ChatRecord> getLastNotCompletedOwnerChatRecord(String ownerChatId) {
        return repository.findLastPendingForOwnerChat(ownerChatId);
    }

    public Optional<ChatRecord> getLastNotCompletedGroupChatRecord(String groupChatId, String participantId) {
        return repository.findFirstByChatIdAndParticipantIdAndCompletedFalseAndActiveTrueOrderByIdDesc(groupChatId, participantId);
    }

    public void update(ChatRecord chatRecord) {
        chatRecord.setUpdatedAt(LocalDateTime.now());
        repository.save(chatRecord);
    }
}
