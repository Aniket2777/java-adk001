package com.shrija.ai.agent.settlement;

import com.google.adk.agents.BaseAgent;
import com.shrija.ai.agent.AgentFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for the Settlement Agent.
 *
 * <p><b>Not yet implemented.</b> This is a deliberate stub, not a silent TODO: the Manager Agent
 * can register this factory now (so orchestration wiring and routing can be built/tested against
 * the full set of department agents), but calling {@link #build()} fails loudly until the
 * Settlement Agent instruction, tools, and business logic are defined.
 *
 * <p>Next step to implement this agent: define its ADK instruction prompt, required tools (e.g.
 * data access for Settlement domain operations), and any sub-agents it delegates to, then replace
 * the exception below with the real LlmAgent construction.
 */
@Component
public class SettlementAgentFactory implements AgentFactory {

  @Override
  public String agentId() {
    return "settlement-agent";
  }

  @Override
  public BaseAgent build() {
    throw new UnsupportedOperationException(
        "Settlement Agent is not yet implemented. See SettlementAgentFactory for scope.");
  }
}
