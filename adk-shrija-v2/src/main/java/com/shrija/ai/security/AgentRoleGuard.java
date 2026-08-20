package com.shrija.ai.security;

import com.google.adk.agents.Callbacks;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.Optional;
import java.util.Set;

public final class AgentRoleGuard {
  private AgentRoleGuard() {}

  public static Callbacks.BeforeAgentCallbackSync requireRoles(Set<String> allowedRoles) {
    return context -> {
      Object roleValue = context.state().get("authenticatedRole");
      String role = roleValue == null ? null : roleValue.toString();
      if (role != null && allowedRoles.contains(role)) {
        return Optional.empty();
      }
      return Optional.of(
          Content.builder()
              .role("model")
              .parts(
                  ImmutableList.of(
                      Part.builder()
                          .text(
                              "Access denied. Your authenticated role is not allowed to use this agent.")
                          .build()))
              .build());
    };
  }
}
