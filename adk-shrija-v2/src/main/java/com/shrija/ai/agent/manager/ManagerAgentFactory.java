package com.shrija.ai.agent.manager;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.shrija.ai.a2a.EmployeeAgentClient;
import com.shrija.ai.agent.AgentFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ManagerAgentFactory implements AgentFactory {
  private static final Logger log = LoggerFactory.getLogger(ManagerAgentFactory.class);

  private static final String INSTRUCTION =
      """
                  You are the Orchestration Agent for an enterprise employee-management assistant.
                  ... (unchanged from your existing INSTRUCTION) ...
                  """;

  private final Gemini geminiModel;
  private final List<AgentFactory> departmentAgentFactories;
  private final EmployeeAgentClient employeeAgentClient;

  public ManagerAgentFactory(
      Gemini geminiModel,
      List<AgentFactory> departmentAgentFactories,
      EmployeeAgentClient employeeAgentClient) {
    this.geminiModel = geminiModel;
    this.employeeAgentClient = employeeAgentClient;
    this.departmentAgentFactories =
        departmentAgentFactories.stream()
            .filter(factory -> !(factory instanceof ManagerAgentFactory))
            // Employee Agent is now a remote A2A microservice, not built in-process.
            // Exclude the old in-process EmployeeAgentFactory so it isn't double-registered.
            .filter(factory -> !factory.agentId().equals("employee-agent"))
            .toList();
  }

  @Override
  public String agentId() {
    return "manager-agent";
  }

  @Override
  public BaseAgent build() {
    List<BaseAgent> subAgents = new ArrayList<>();

    // Remaining in-process department agents (Attendance, Payroll, HR — for now).
    departmentAgentFactories.forEach(
        factory -> {
          try {
            subAgents.add(factory.build());
          } catch (UnsupportedOperationException ex) {
            log.warn("Skipping agent '{}': {}", factory.agentId(), ex.getMessage());
          }
        });

    // Employee Agent — now a remote A2A microservice.
    try {
      subAgents.add(employeeAgentClient.connect());
    } catch (IllegalStateException ex) {
      log.warn("Employee Agent (A2A) unavailable, skipping: {}", ex.getMessage());
    }

    return LlmAgent.builder()
        .name(agentId())
        .description(
            "LLM orchestration agent that semantically routes requests to Employee, Attendance, Payroll or HR agents.")
        .instruction(INSTRUCTION)
        .model(geminiModel)
        .subAgents(subAgents)
        .build();
  }
}
