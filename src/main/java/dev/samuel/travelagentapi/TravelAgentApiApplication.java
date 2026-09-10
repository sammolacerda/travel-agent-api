package dev.samuel.travelagentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TravelProperties.class)
public class TravelAgentApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                TravelAgentApiApplication.class,
                args
        );
    }
}