package com.shrija.ai.agent.attendance;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.ai.agent.AgentFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAgentFactory implements AgentFactory {
  private final Gemini geminiModel;
  private final McpToolset attendanceMcpToolset;

  public AttendanceAgentFactory(
      Gemini geminiModel, @Qualifier("attendanceMcpToolset") McpToolset attendanceMcpToolset) {
    this.geminiModel = geminiModel;
    this.attendanceMcpToolset = attendanceMcpToolset;
  }

  @Override
  public String agentId() {
    return "attendance-agent";
  }

  @Override
  public BaseAgent build() {
    return LlmAgent.builder()
        .name(agentId())
        .description(
            "Handles employee attendance, check-in, check-out and attendance history using MCP.")
        .instruction(
            """
                        You are the Attendance Agent.
                        Handle attendance requests only.
                        For self-service requests, use the authenticated employeeCode from the context.
                        Never use an employee code supplied by the user if it conflicts with authenticated context.
                        Use MCP tools for attendance data. Never access the database directly.
                        """)
        .model(geminiModel)
        .tools(ImmutableList.of(attendanceMcpToolset))
        .build();
  }
}
