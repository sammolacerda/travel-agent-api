package dev.samuel.travelagentapi;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;

@Configuration
public class McpClientConfig {

    @Bean
    McpClient travelMcpClient(
            TravelProperties properties
    ) {

        var mcp = properties.mcp();

        var transport =
                new StdioMcpTransport.Builder()
                        .command(
                                List.of(
                                        mcp.command(),
                                        "-jar",
                                        mcp.jar()
                                )
                        )
                        .build();

        return new DefaultMcpClient.Builder()
                .key("travel-mcp-server")
                .transport(transport)
                .build();
    }

    @Bean
    ToolProvider toolProvider(
            McpClient travelMcpClient
    ) {

        return McpToolProvider.builder()
                .mcpClients(
                        List.of(travelMcpClient)
                )
                .build();
    }
}