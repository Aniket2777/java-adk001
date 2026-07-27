package com.shrija.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound payload for {@code POST /api/v1/chat}.
 *
 * @param userId caller identity (will come from the JWT principal once security is wired in;
 *     accepted directly for now so the endpoint is usable before auth is implemented)
 * @param sessionId optional - omit or leave blank to start a new conversation
 * @param message the user's message
 */
public record ChatRequest(@NotBlank String userId, String sessionId, @NotBlank String message) {}
