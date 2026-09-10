package dev.samuel.travelagentapi.memory.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.samuel.travelagentapi.memory.persistence.entity.MessageEntity;

import java.util.List;
import java.util.UUID;

public interface MessageRepository
        extends JpaRepository<MessageEntity, UUID> {

    List<MessageEntity> findByConversationIdOrderByCreatedAtAsc(
            UUID conversationId
    );

    void deleteByConversationId(UUID conversationId);
}
