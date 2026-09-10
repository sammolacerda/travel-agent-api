package dev.samuel.travelagentapi;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travel")
public record TravelProperties(
        Ai ai,
        Mcp mcp
) {

    public record Ai(
            Ollama ollama,
            Langchain4j langchain4j
    ) {
    }

    public record Ollama(
            String baseUrl,
            String model,
            Double temperature,
            Duration timeout
    ) {
    }

    public record Langchain4j(
            boolean logRequests,
            boolean logResponses
    ) {
    }

    public record Mcp(
            String command,
            String jar
    ) {
    }
}