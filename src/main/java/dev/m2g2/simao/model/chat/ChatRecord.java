package dev.m2g2.simao.model.chat;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "chat_record", schema = "public")
public class ChatRecord extends BaseModel {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Interaction<?> interaction;

    private boolean completed = false;

    /**
     * Chat the pending interaction belongs to (owner's private chat id, or the
     * purchases group id). Null on rows written before this column existed —
     * those are treated as belonging to the owner's private chat.
     */
    @Column(name = "chat_id")
    private String chatId;

    /**
     * Sender identity within chatId. Always null for the owner's private chat
     * (only the owner is ever a valid sender there); holds the WAHA participant
     * id for purchases-group rows so different group members don't share state.
     */
    @Column(name = "participant_id")
    private String participantId;

    public Interaction<?> getInteraction() {
        return interaction;
    }

    public void setInteraction(Interaction<?> interaction) {
        this.interaction = interaction;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
}
