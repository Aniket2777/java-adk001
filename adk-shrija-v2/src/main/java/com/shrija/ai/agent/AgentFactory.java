package com.shrija.ai.agent;

import com.google.adk.agents.BaseAgent;

/**
 * Contract for every department agent factory (HR, Payroll, Budget, Settlement, Report,
 * Notification, Employee, and the Manager itself).
 *
 * <p>Each implementation is responsible only for building its own {@link BaseAgent} instance (name,
 * instruction/prompt, tools, sub-agents). Wiring agents together is the Manager Agent's job, not
 * each factory's — this keeps every agent independently testable per the project's AI Agent
 * Guidelines.
 *
 * <p><b>Verify against your local {@code core} module:</b> {@code BaseAgent} is used here as the
 * common ADK agent supertype based on the {@code com.google.adk.agents} package you showed me;
 * confirm the exact class name (e.g. it may be {@code LlmAgent} directly, or {@code Agent}).
 */
public interface AgentFactory {

  /**
   * Unique, stable identifier for this agent (used for routing and logging). Convention:
   * lower-kebab-case, e.g. {@code "hr-agent"}.
   */
  String agentId();

  /**
   * Builds a fresh agent instance. Implementations must not cache state across calls — session
   * state belongs in ADK's session service, not in the factory.
   */
  BaseAgent build();
}
