package dev.samuel.travel_agent_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.tool.ToolProvider;
import dev.samuel.travelagentapi.TravelAgentApiApplication;

@SpringBootTest(
		classes = TravelAgentApiApplication.class,
		properties = "spring.datasource.url=jdbc:h2:mem:travel-agent-test;DB_CLOSE_DELAY=-1"
)
class TravelAgentApiApplicationTests {

	@MockitoBean
	McpClient travelMcpClient;

	@MockitoBean
	ToolProvider toolProvider;

	@MockitoBean
	ChatModel chatModel;

	@Test
	void contextLoads() {
	}

}
