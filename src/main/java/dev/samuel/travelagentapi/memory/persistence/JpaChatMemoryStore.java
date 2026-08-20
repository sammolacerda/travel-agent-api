package dev.samuel.travelagentapi.memory.persistence;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.samuel.travelagentapi.memory.persistence.entity.ConversationEntity;
import dev.samuel.travelagentapi.memory.persistence.entity.MessageEntity;
import dev.samuel.travelagentapi.memory.persistence.entity.domain.MessageRole;
import dev.samuel.travelagentapi.memory.persistence.entity.repository.ConversationRepository;
import dev.samuel.travelagentapi.memory.persistence.entity.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class JpaChatMemoryStore implements ChatMemoryStore {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public JpaChatMemoryStore(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Object memoryId) {
        var conversationId = conversationId(memoryId);

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toChatMessage)
                .toList();
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        var conversationId = conversationId(memoryId);
        var conversation = conversationRepository.findById(conversationId)
                .orElseGet(() -> new ConversationEntity(conversationId));

        conversation.touch();
        conversationRepository.save(conversation);
        messageRepository.deleteByConversationId(conversationId);
        messageRepository.saveAll(messages.stream()
                .map(message -> new MessageEntity(
                        conversationId,
                        roleOf(message),
                        textOf(message),
                        ChatMessageSerializer.messageToJson(message)
                ))
                .toList());
    }

    @Override
    @Transactional
    public void deleteMessages(Object memoryId) {
        messageRepository.deleteByConversationId(conversationId(memoryId));
    }

    private ChatMessage toChatMessage(MessageEntity entity) {
        if (entity.getSerializedMessage() != null) {
            return ChatMessageDeserializer.messageFromJson(entity.getSerializedMessage());
        }

        return switch (entity.getRole()) {
            case USER -> UserMessage.from(entity.getContent());
            case ASSISTANT -> AiMessage.from(entity.getContent());
        };
    }

    private MessageRole roleOf(ChatMessage message) {
        // Older H2 databases constrain this column to USER and ASSISTANT.
        // The exact message type is retained in serialized_message.
        return message.type() == dev.langchain4j.data.message.ChatMessageType.USER
                ? MessageRole.USER
                : MessageRole.ASSISTANT;
    }

    private String textOf(ChatMessage message) {
        return switch (message.type()) {
            case USER -> ((UserMessage) message).singleText();
            case AI -> ((AiMessage) message).text() == null ? "" : ((AiMessage) message).text();
            case SYSTEM -> ((SystemMessage) message).text();
            default -> ChatMessageSerializer.messageToJson(message);
        };
    }

    private UUID conversationId(Object memoryId) {
        if (memoryId instanceof UUID id) {
            return id;
        }
        return UUID.fromString(memoryId.toString());
    }
}
