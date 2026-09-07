package com.shrija.payroll.exception;

/**
 * Thrown when a conversation turn fails because the agent could not be built or could not complete
 * execution (model error, MCP tool failure, etc.). Maps to HTTP 502 in {@code
 * GlobalExceptionHandler} - the client's request was well-formed, but the agent layer could not
 * fulfill it.
 */
public class AgentExecutionException extends ShrijaPayrollException {

  public AgentExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
