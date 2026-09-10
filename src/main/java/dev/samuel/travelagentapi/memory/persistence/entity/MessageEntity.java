package dev.samuel.travelagentapi.memory.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.samuel.travelagentapi.memory.persistence.entity.domain.MessageRole;

@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(
            name = "idx_messages_conversation_created_at",
            columnList = "conversation_id, created_at"
        )
    }
)
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    @Column(name = "serialized_message")
    private String serializedMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MessageEntity() {
    }

    public MessageEntity(
            UUID conversationId,
            MessageRole role,
            String content,
            String serializedMessage
    ) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.serializedMessage = serializedMessage;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getSerializedMessage() {
        return serializedMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
