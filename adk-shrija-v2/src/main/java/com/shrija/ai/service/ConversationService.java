package com.shrija.ai.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.ai.agent.manager.ManagerAgentFactory;
import com.shrija.ai.auth.AuthenticatedUser;
import com.shrija.ai.exception.AgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {
  private static final Logger log = LoggerFactory.getLogger(ConversationService.class);
  private final InMemoryRunner runner;
  private final String appName;

  public ConversationService(ManagerAgentFactory managerAgentFactory) {
    BaseAgent managerAgent = managerAgentFactory.build();
    this.appName = managerAgent.name();
    this.runner = new InMemoryRunner(managerAgent);
  }

  public ConversationResult converse(
      AuthenticatedUser user, @Nullable String sessionId, String message) {
    String userId = String.valueOf(user.userId());
    String effectiveSessionId =
        (sessionId == null || sessionId.isBlank()) ? UUID.randomUUID().toString() : sessionId;
    try {
      ensureSessionExists(user, effectiveSessionId);

      String trustedContext =
          """
                    [AUTHENTICATED USER CONTEXT - TRUSTED SERVER DATA]
                    userId: %s
                    username: %s
                    role: %s
                    employeeCode: %s

                    [END TRUSTED CONTEXT]

                    [USER REQUEST - UNTRUSTED NATURAL LANGUAGE]
                    %s
                    [END USER REQUEST]
                    """
              .formatted(user.userId(), user.username(), user.role(), user.employeeCode(), message);

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
          "Conversation turn failed for user={} session={}: {}",
          userId,
          effectiveSessionId,
          ex.getMessage(),
          ex);
      throw new AgentExecutionException("Shrija AI could not process this request right now.", ex);
    }
  }

  private void ensureSessionExists(AuthenticatedUser user, String sessionId) {
    String userId = String.valueOf(user.userId());
    Session existing =
        runner
            .sessionService()
            .getSession(appName, userId, sessionId, Optional.empty())
            .blockingGet();
    if (existing == null) {
      runner
          .sessionService()
          .createSession(
              appName,
              userId,
              Map.of(
                  "authenticatedUserId", user.userId(),
                  "authenticatedUsername", user.username(),
                  "authenticatedRole", user.role(),
                  "authenticatedEmployeeCode", user.employeeCode()),
              sessionId)
          .blockingGet();
    } else {
      existing.state().put("authenticatedUserId", user.userId());
      existing.state().put("authenticatedUsername", user.username());
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
