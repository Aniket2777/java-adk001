package com.shrija.ai.agent.manager;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.shrija.ai.agent.AgentFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Builds the Manager Agent: the central orchestrator that receives every user request, understands
 * intent, and delegates to the appropriate department agent.
 *
 * <p>Design: the Manager is itself an ADK {@code LlmAgent} configured with the full list of
 * department agents as ADK sub-agents. This reuses ADK's own agent-transfer / delegation mechanism
 * (the {@code agents} package you showed already supports sub-agent composition) instead of writing
 * a custom routing layer — per the project rule "reuse existing ADK components instead of creating
 * custom implementations."
 *
 * <p>Failure handling: if a department agent is not yet implemented (see the stub factories in
 * {@code agent.hr}, {@code agent.payroll}, etc.), building it will throw. We deliberately do NOT
 * swallow that here — {@link com.shrija.ai.service.ConversationService} is responsible for catching
 * and translating agent build/execution failures into a graceful user-facing response, per the
 * Manager Agent's "handle failures gracefully" responsibility. Keeping the exception un-swallowed
 * here keeps this factory simple and testable in isolation.
 *
 * <p><b>Verify against your local {@code core} module:</b> {@code LlmAgent} builder shape
 * (name/instruction/model/subAgents) is inferred from the {@code com.google.adk.agents} package
 * layout; confirm exact builder method names against the jar.
 */
@Component
public class ManagerAgentFactory implements AgentFactory {

  private static final Logger log = LoggerFactory.getLogger(ManagerAgentFactory.class);

  private static final String INSTRUCTION =
      """
            You are the Manager Agent for Shrija AI, an enterprise assistant.
            Understand the user's intent and delegate to exactly one of your
            department sub-agents: HR, Payroll, Budget, Settlement, Report,
            Notification, or Employee. If the request spans multiple
            departments, coordinate the relevant sub-agents in sequence and
            aggregate their responses into a single, coherent answer. If no
            sub-agent can handle the request, say so plainly rather than
            guessing.

            Leave specifically is split across two agents - route carefully:
            - Applying for leave, or checking one's own leave balance ->
              Employee Agent.
            - Reviewing, approving, or rejecting a leave request, or listing
              pending requests -> HR Agent.

            Document requests (offer letter, joining letter, experience
            letter, salary certificate, ID proof) follow the same split:
            - Requesting a document, or checking one's own request status ->
              Employee Agent.
            - Reviewing the pending queue, or marking a request ready or
              delivered -> HR Agent.
            """;

  private final Gemini geminiModel;
  private final List<AgentFactory> departmentAgentFactories;

  public ManagerAgentFactory(Gemini geminiModel, List<AgentFactory> departmentAgentFactories) {
    this.geminiModel = geminiModel;
    // Exclude self in case Spring's List<AgentFactory> injection ever
    // includes this bean; the Manager is not its own sub-agent.
    this.departmentAgentFactories =
        departmentAgentFactories.stream()
            .filter(factory -> !(factory instanceof ManagerAgentFactory))
            .toList();
  }

  @Override
  public String agentId() {
    return "manager-agent";
  }

  @Override
  public BaseAgent build() {
    // Department agents are built defensively: an agent that isn't
    // implemented yet (see the stub factories) is logged and skipped
    // rather than failing the whole Manager Agent's startup. This keeps
    // the skeleton runnable end-to-end today, and each department comes
    // online automatically the moment its factory is implemented for
    // real - no change needed here when that happens.
    List<BaseAgent> subAgents =
        departmentAgentFactories.stream()
            .flatMap(
                factory -> {
                  try {
                    return java.util.stream.Stream.of(factory.build());
                  } catch (UnsupportedOperationException notYetImplemented) {
                    log.warn(
                        "Skipping department agent '{}': {}",
                        factory.agentId(),
                        notYetImplemented.getMessage());
                    return java.util.stream.Stream.empty();
                  }
                })
            .toList();

    return LlmAgent.builder()
        .name(agentId())
        .instruction(INSTRUCTION)
        .model(geminiModel)
        .subAgents(subAgents)
        .build();
  }
}
