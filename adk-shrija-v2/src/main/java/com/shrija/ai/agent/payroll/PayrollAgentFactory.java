package com.shrija.ai.agent.payroll;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.ai.agent.AgentFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PayrollAgentFactory implements AgentFactory {
  private final Gemini geminiModel;
  private final McpToolset payrollMcpToolset;

  public PayrollAgentFactory(
      Gemini geminiModel, @Qualifier("payrollMcpToolset") McpToolset payrollMcpToolset) {
    this.geminiModel = geminiModel;
    this.payrollMcpToolset = payrollMcpToolset;
  }

  @Override
  public String agentId() {
    return "payroll-agent";
  }

  @Override
  public BaseAgent build() {
    return LlmAgent.builder()
        .name(agentId())
        .description(
            "Handles employee salary, payslip, payroll history and latest salary requests using MCP.")
        .instruction(
            """
                        You are the Payroll Agent.
                        Handle payroll and salary requests only.
                        For self-service requests, use the authenticated employeeCode from the context.
                        Never expose another employee's payroll to an employee unless the authenticated role permits it.
                        Use MCP tools for payroll data. Never access the database directly.
                        You are read-only in this implementation.
                        """)
        .model(geminiModel)
        .tools(ImmutableList.of(payrollMcpToolset))
        .build();
  }
}
