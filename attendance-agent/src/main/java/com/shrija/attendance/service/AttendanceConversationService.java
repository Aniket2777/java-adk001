package com.shrija.attendance.service;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.attendance.agent.AttendanceAgent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AttendanceConversationService {

  private final InMemoryRunner runner;

  public AttendanceConversationService(AttendanceAgent attendanceAgent) {
    this.runner = new InMemoryRunner(attendanceAgent.agent());
  }

  public ConversationResult converse(String userId, String sessionId, String message) {
    String effectiveUserId = userId == null || userId.isBlank() ? "anonymous" : userId;
    String effectiveSessionId =
        sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString() : sessionId;

    ensureSession(effectiveUserId, effectiveSessionId);

    Content userMessage =
        Content.builder()
            .role("user")
            .parts(ImmutableList.of(Part.builder().text(message).build()))
            .build();

    List<Event> events =
        runner
            .runAsync(effectiveUserId, effectiveSessionId, userMessage, RunConfig.builder().build())
            .toList()
            .blockingGet();

    StringBuilder response = new StringBuilder();
    for (Event event : events) {
      response.append(event.stringifyContent());
    }
    return new ConversationResult(effectiveSessionId, response.toString().stripTrailing());
  }

  private void ensureSession(String userId, String sessionId) {
    Session existing =
        runner
            .sessionService()
            .getSession(runner.appName(), userId, sessionId, Optional.empty())
            .blockingGet();
    if (existing == null) {
      runner
          .sessionService()
          .createSession(runner.appName(), userId, null, sessionId)
          .blockingGet();
    }
  }

  public record ConversationResult(String sessionId, String responseText) {}
}
