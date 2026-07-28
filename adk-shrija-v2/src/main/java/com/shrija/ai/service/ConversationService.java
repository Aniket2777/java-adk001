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
import com.shrija.ai.exception.AgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Single entry point the REST layer calls to run one conversation turn through the Manager Agent.
 *
 * <p><b>Why the Runner is built once, in the constructor, and reused:</b> {@link InMemoryRunner}
 * owns its own {@code InMemorySessionService} internally. Building a new one per request (as an
 * earlier draft of this class did) silently discards every session's history each time - the {@code
 * sessionId} returned to the caller would never resolve to anything on the next call. This is
 * in-memory only, so state is still lost on restart; swapping in a persistent {@code
 * BaseSessionService} later only requires changing the constructor below, not this class's public
 * API.
 *
 * <p>Session creation is explicit (mirroring ADK's own {@code HelloWorldRun} sample) rather than
 * relying on {@code RunConfig.autoCreateSession()}, which defaults to {@code false}.
 */
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

  /**
   * Runs a single message through the Manager Agent for the given session, creating a new session
   * id when {@code sessionId} is blank.
   *
   * @param userId identifies the caller for session scoping and audit logging
   * @param sessionId existing session id to continue a conversation, or blank for a new one
   * @param message the user's message text
   * @return the agent's final text response plus the session id to reuse on the next call
   */
  public ConversationResult converse(String userId, @Nullable String sessionId, String message) {
    String effectiveSessionId =
        (sessionId == null || sessionId.isBlank()) ? UUID.randomUUID().toString() : sessionId;

    try {
      ensureSessionExists(userId, effectiveSessionId);

      Content userMessage =
          Content.builder()
              .role("user")
              .parts(ImmutableList.of(Part.builder().text(message).build()))
              .build();

      Flowable<Event> events =
          runner.runAsync(userId, effectiveSessionId, userMessage, RunConfig.builder().build());

      String responseText = collectFinalText(events);

      return new ConversationResult(effectiveSessionId, responseText);
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

  private void ensureSessionExists(String userId, String sessionId) {
    Session existing =
        runner
            .sessionService()
            .getSession(appName, userId, sessionId, Optional.empty())
            .blockingGet(); // null when no session exists - Maybe.blockingGet() contract
    if (existing == null) {
      runner.sessionService().createSession(appName, userId, null, sessionId).blockingGet();
    }
  }

  private String collectFinalText(Flowable<Event> events) {
    List<Event> collected = events.toList().blockingGet();
    StringBuilder sb = new StringBuilder();
    for (Event event : collected) {
      sb.append(event.stringifyContent());
    }
    return sb.toString().stripTrailing();
  }

  /**
   * Result of a single conversation turn.
   *
   * @param sessionId session id to pass on the next call to continue this conversation
   * @param responseText the Manager Agent's aggregated response text
   */
  public record ConversationResult(String sessionId, String responseText) {

  }
}
