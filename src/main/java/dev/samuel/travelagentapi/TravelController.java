package dev.samuel.travelagentapi;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        var response =
                travelAgent.chat(
                        request.message()
                );

        return new TravelResponse(
                response
        );
    }

    public record TravelRequest(
            String message
    ) {
    }

    public record TravelResponse(
            String response
    ) {
    }
}