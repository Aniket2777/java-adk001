package com.shrija.payroll.dto;

/**
 * Outbound payload for {@code POST /api/v1/payroll/chat}.
 *
 * @param sessionId pass this back on the next request to continue the conversation
 * @param message the Payroll Agent's response
 */
public record ChatResponse(String sessionId, String message) {}
