package com.shrija.ai.agent.hr;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.ai.agent.AgentFactory;
import com.shrija.ai.prompts.HrAgentPrompts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Builds the HR Agent. All three of its responsibilities (employee directory, leave approval,
 * document fulfillment) now come from one source: the {@code hrMcpToolset} bean, an {@link
 * McpToolset} client connected to {@code mcp-shrija-server} and filtered to HR's allowed tool names
 * (see {@code McpToolsetConfig}). No direct database access, no direct service calls - every
 * DB-touching operation goes through MCP, per the explicit requirement.
 */
@Component
public class HrAgentFactory implements AgentFactory {

  private final Gemini geminiModel;
  private final McpToolset hrMcpToolset;

  public HrAgentFactory(Gemini geminiModel, @Qualifier("hrMcpToolset") McpToolset hrMcpToolset) {
    this.geminiModel = geminiModel;
    this.hrMcpToolset = hrMcpToolset;
  }

  @Override
  public String agentId() {
    return "hr-agent";
  }

  @Override
  public BaseAgent build() {
    return LlmAgent.builder()
        .name(agentId())
        .description(
            "Answers questions about employee records, adds/removes/transfers "
                + "employees, reviews pending leave requests (approve/reject), and fulfills "
                + "document requests (offer letter, joining letter, experience letter, "
                + "salary certificate, ID proof) - all via MCP tools backed by the shared "
                + "database, never directly.")
        .instruction(HrAgentPrompts.HR_AGENT_INSTRUCTION)
        .model(geminiModel)
        .tools(ImmutableList.of(hrMcpToolset))
        .build();
  }
}
