package dev.m2g2.simao.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;
    // Preenchidos pelo AuditorProvider a partir de quem está autenticado.
    // Ficam nulos no que é gravado sem sessão — a semeadura do primeiro admin.
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by_id", updatable = false)
    protected User createdBy;
    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    protected User updatedBy;
    protected boolean active = true;

    /**
     * Carimba as datas na própria persistência, em vez de depender de cada
     * service lembrar. createdAt só é preenchido quando ainda está vazio, para
     * que a importação de projetos antigos consiga preservar a data original.
     */
    @PrePersist
    protected void onPersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null)
            createdAt = now;
        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
