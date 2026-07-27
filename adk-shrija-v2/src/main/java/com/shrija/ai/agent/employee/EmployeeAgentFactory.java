package com.shrija.ai.agent.employee;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.ai.agent.AgentFactory;
import com.shrija.ai.prompts.EmployeeAgentPrompts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Builds the Employee Agent: self-service (leave, onboarding/offboarding status, document
 * requests), backed by the {@code employeeMcpToolset} bean - the same MCP server HR talks to,
 * filtered (see {@code McpToolsetConfig}) to only the employee-side tool names, so this agent has
 * no way to reach {@code approveLeaveRequest} or any other HR-only action even by accident.
 */
@Component
public class EmployeeAgentFactory implements AgentFactory {

  private final Gemini geminiModel;
  private final McpToolset employeeMcpToolset;

  public EmployeeAgentFactory(
      Gemini geminiModel, @Qualifier("employeeMcpToolset") McpToolset employeeMcpToolset) {
    this.geminiModel = geminiModel;
    this.employeeMcpToolset = employeeMcpToolset;
  }

  @Override
  public String agentId() {
    return "employee-agent";
  }

  @Override
  public BaseAgent build() {
    return LlmAgent.builder()
        .name(agentId())
        .description(
            "Handles employee self-service: leave balance and applications, "
                + "onboarding/offboarding status, and document requests - all via MCP "
                + "tools backed by the shared database, never directly.")
        .instruction(EmployeeAgentPrompts.EMPLOYEE_AGENT_INSTRUCTION)
        .model(geminiModel)
        .tools(ImmutableList.of(employeeMcpToolset))
        .build();
  }
}
