package dev.samuel.travelagentapi.config;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.MemoryId;

import java.util.UUID;

public interface TravelAgent {

    @SystemMessage("""
            You are a travel planning agent.

            Your goal is to help users find economical travel options.

            You have tools available to search flights and hotels.

            Whenever the user asks about:
            - flight prices
            - flights
            - hotels
            - hotel prices

            use the available tools.

            Never invent prices.

            When multiple options are available,
            compare them and prefer cheaper alternatives
            when the user asks to save money.

            Respond in the same language as the user.
            """)
    String chat(
            @MemoryId UUID conversationId,
            @UserMessage String message
    );
}
