package com.shrija.ai.agent.employee;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.ai.agent.AgentFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
            "Handles employee self-service: profile, leave balance/history/applications and document-related requests using MCP.")
        .instruction(
            """
                        You are the Employee Agent.
                        Handle employee self-service requests only.
                        The authenticated employeeCode is supplied by the Orchestration Agent context.
                        For a request about the current user, always use that authenticated employeeCode.
                        Do not trust a different employeeCode supplied inside the user's text.
                        Use MCP tools for all data access. Never access the database directly.
                        Do not perform HR-only actions such as approving leave or terminating employees.
                        """)
        .model(geminiModel)
        .tools(ImmutableList.of(employeeMcpToolset))
        .build();
  }
}
