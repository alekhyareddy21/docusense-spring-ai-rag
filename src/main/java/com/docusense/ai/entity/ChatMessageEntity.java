package com.docusense.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String role; // "USER" or "ASSISTANT"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String retrievedSources;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private ChatSessionEntity session;

    public ChatMessageEntity() {}

    public ChatMessageEntity(Long id, String role, String content, String retrievedSources,
                             LocalDateTime createdAt, ChatSessionEntity session) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.retrievedSources = retrievedSources;
        this.createdAt = createdAt;
        this.session = session;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String role;
        private String content;
        private String retrievedSources;
        private LocalDateTime createdAt;
        private ChatSessionEntity session;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder retrievedSources(String retrievedSources) { this.retrievedSources = retrievedSources; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder session(ChatSessionEntity session) { this.session = session; return this; }

        public ChatMessageEntity build() {
            return new ChatMessageEntity(id, role, content, retrievedSources, createdAt, session);
        }
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRetrievedSources() { return retrievedSources; }
    public void setRetrievedSources(String retrievedSources) { this.retrievedSources = retrievedSources; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ChatSessionEntity getSession() { return session; }
    public void setSession(ChatSessionEntity session) { this.session = session; }
}
