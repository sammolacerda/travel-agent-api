package dev.samuel.travelagentapi.memory.persistence.entity.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.samuel.travelagentapi.memory.persistence.entity.ConversationEntity;

import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<ConversationEntity, UUID> {
}
