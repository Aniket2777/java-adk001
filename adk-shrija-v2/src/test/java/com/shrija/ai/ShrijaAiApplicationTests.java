package com.shrija.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.shrija.ai.agent.manager.ManagerAgentFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test: confirms the Spring context loads with all agent factories wired (including the seven
 * not-yet-implemented department stubs), and that the Manager Agent bean itself resolves.
 * Deliberately does not call {@code build()} on the manager here - that requires a live Gemini API
 * key and is covered by an integration test once the first department agent is implemented for
 * real.
 */
@SpringBootTest
@TestPropertySource(
    properties = {
      "shrija.ai.gemini-api-key=test-key",
      "shrija.ai.gemini-model=gemini-2.5-flash",
      "shrija.ai.jwt.secret=test-secret-test-secret-test-secret",
      "shrija.ai.mcp-server-url=http://localhost:8082"
    })
class ShrijaAiApplicationTests {

  @Autowired private ManagerAgentFactory managerAgentFactory;

  @Test
  void contextLoads() {
    assertNotNull(managerAgentFactory);
  }
}
