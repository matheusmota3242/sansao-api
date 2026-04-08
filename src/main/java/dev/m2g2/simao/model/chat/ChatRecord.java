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
}
