package dev.samuel.travelagentapi;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AiConfig {

    @Bean
    ChatModel chatModel(
            TravelProperties properties
    ) {

        var ollama =
                properties.ai().ollama();

        var langchain =
                properties.ai().langchain4j();

        return OllamaChatModel.builder()
                .baseUrl(ollama.baseUrl())
                .modelName(ollama.model())
                .temperature(ollama.temperature())
                .timeout(ollama.timeout())
                .logRequests(langchain.logRequests())
                .logResponses(langchain.logResponses())
                .build();
    }

    @Bean
    TravelAgent travelAgent(
            ChatModel chatModel,
            ToolProvider toolProvider,
            ChatMemoryStore chatMemoryStore
    ) {

        return AiServices.builder(
                        TravelAgent.class
                )
                .chatModel(chatModel)
                .toolProvider(toolProvider)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(100)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();
    }
}
