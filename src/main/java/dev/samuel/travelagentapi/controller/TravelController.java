package dev.samuel.travelagentapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.samuel.travelagentapi.config.TravelAgent;

import java.util.UUID;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    private final TravelAgent travelAgent;

    public TravelController(
            TravelAgent travelAgent
    ) {

        this.travelAgent = travelAgent;
    }

    @PostMapping
    public TravelResponse chat(
            @RequestBody TravelRequest request
    ) {

        var conversationId = request.conversationId() == null
                ? UUID.randomUUID()
                : request.conversationId();

        var response =
                travelAgent.chat(
                        conversationId,
                        request.message()
                );

        return new TravelResponse(
                conversationId,
                response
        );
    }

    public record TravelRequest(
            UUID conversationId,
            String message
    ) {
    }

    public record TravelResponse(
            UUID conversationId,
            String response
    ) {
    }
}
