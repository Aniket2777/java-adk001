package com.shrija.payroll.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.payroll.agent.PayrollAgentFactory;
import com.shrija.payroll.auth.AuthenticatedUser;
import com.shrija.payroll.exception.AgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Nullable;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs conversation turns directly against the Payroll Agent. Unlike {@code ConversationService} in
 * adk-shrija-v2 (which routes through a Manager/orchestrator agent that fans out to several
 * department agents), this standalone module hosts exactly one agent, so the Payroll Agent itself
 * is the runner's root agent.
 */
@Service
public class PayrollConversationService {
  private static final Logger log = LoggerFactory.getLogger(PayrollConversationService.class);
  private final InMemoryRunner runner;
  private final String appName;

  public PayrollConversationService(PayrollAgentFactory payrollAgentFactory) {
    BaseAgent payrollAgent = payrollAgentFactory.build();
    this.appName = payrollAgent.name();
    this.runner = new InMemoryRunner(payrollAgent);
  }

  public ConversationResult converse(
      AuthenticatedUser user, @Nullable String sessionId, String message) {
    String userId = user.userId();
    String effectiveSessionId =
        (sessionId == null || sessionId.isBlank()) ? UUID.randomUUID().toString() : sessionId;
    try {
      ensureSessionExists(user, effectiveSessionId);

      String trustedContext =
          """
              [AUTHENTICATED USER CONTEXT - TRUSTED SERVER DATA]
              userId: %s
              role: %s
              employeeCode: %s

              [END TRUSTED CONTEXT]

              [USER REQUEST - UNTRUSTED NATURAL LANGUAGE]
              %s
              [END USER REQUEST]
              """
              .formatted(user.userId(), user.role(), user.employeeCode(), message);

      Content userMessage =
          Content.builder()
              .role("user")
              .parts(ImmutableList.of(Part.builder().text(trustedContext).build()))
              .build();

      Flowable<Event> events =
          runner.runAsync(userId, effectiveSessionId, userMessage, RunConfig.builder().build());
      return new ConversationResult(effectiveSessionId, collectFinalText(events));
    } catch (Exception ex) {
      log.error(
          "Payroll conversation turn failed for user={} session={}: {}",
          userId,
          effectiveSessionId,
          ex.getMessage(),
          ex);
      throw new AgentExecutionException(
          "Payroll Agent could not process this request right now.", ex);
    }
  }

  private void ensureSessionExists(AuthenticatedUser user, String sessionId) {
    String userId = user.userId();
    Session existing =
            runner
                    .sessionService()
                    .getSession(appName, userId, sessionId, Optional.empty())
                    .blockingGet();
    if (existing == null) {
      Map<String, Object> initialState = new HashMap<>();
      initialState.put("authenticatedUserId", user.userId());
      initialState.put("authenticatedRole", user.role());
      initialState.put("authenticatedEmployeeCode", user.employeeCode());
      runner.sessionService().createSession(appName, userId, initialState, sessionId).blockingGet();
    } else {
      existing.state().put("authenticatedUserId", user.userId());
      existing.state().put("authenticatedRole", user.role());
      existing.state().put("authenticatedEmployeeCode", user.employeeCode());
    }
  }

  private String collectFinalText(Flowable<Event> events) {
    List<Event> collected = events.toList().blockingGet();
    StringBuilder sb = new StringBuilder();
    for (Event event : collected) sb.append(event.stringifyContent());
    return sb.toString().stripTrailing();
  }

  public record ConversationResult(String sessionId, String responseText) {}
}
