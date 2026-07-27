package com.shrija.ai.dto;

/**
 * Outbound payload for {@code POST /api/v1/chat}.
 *
 * @param sessionId pass this back on the next request to continue the conversation
 * @param message the Manager Agent's aggregated final response
 */
public record ChatResponse(String sessionId, String message) {}
