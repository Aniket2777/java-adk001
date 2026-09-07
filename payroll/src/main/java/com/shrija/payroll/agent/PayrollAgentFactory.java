package com.shrija.payroll.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.payroll.prompts.PayrollAgentPrompts;
import com.shrija.payroll.security.AgentRoleGuard;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Builds the Payroll Agent. All salary-slip operations (view, generate/regenerate, mark paid) come
 * from one source: the {@code payrollMcpToolset} bean, an {@link McpToolset} client connected to
 * the shared {@code mcp-server} and filtered to payroll's allowed tool names (see {@code
 * McpToolsetConfig}). No direct database access - every DB-touching operation goes through MCP.
 *
 * <p>The agent is reachable by EMPLOYEE (self-service viewing only), HR and ADMIN (full access);
 * that role gate is enforced twice - coarsely here via {@link AgentRoleGuard} (who may talk to this
 * agent at all) and finely inside the instruction/prompt (which roles may trigger the
 * generate/mark-paid tools). The prompt-level gate is not a hard security boundary by itself, so if
 * stricter enforcement is ever needed, add a role check inside the MCP tool implementations
 * themselves (in {@code mcp-server}) rather than relying on the LLM alone.
 */
@Component
public class PayrollAgentFactory {

  private final Gemini geminiModel;
  private final McpToolset payrollMcpToolset;

  public PayrollAgentFactory(
      Gemini geminiModel, @Qualifier("payrollMcpToolset") McpToolset payrollMcpToolset) {
    this.geminiModel = geminiModel;
    this.payrollMcpToolset = payrollMcpToolset;
  }

  public String agentId() {
    return "payroll-agent";
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name(agentId())
        .description(
            "Handles salary-slip requests: viewing (self-service for employees, any "
                + "employee for HR/ADMIN), generating/regenerating, and marking "
                + "paid - all via MCP tools backed by the shared database, never "
                + "directly.")
        .instruction(PayrollAgentPrompts.PAYROLL_AGENT_INSTRUCTION)
        .model(geminiModel)
        .beforeAgentCallbackSync(AgentRoleGuard.requireRoles(Set.of("EMPLOYEE", "HR", "ADMIN")))
        .tools(ImmutableList.of(payrollMcpToolset))
        .build();
  }
}
