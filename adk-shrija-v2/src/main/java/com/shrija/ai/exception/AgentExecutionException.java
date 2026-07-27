package com.shrija.ai.exception;

/**
 * Thrown when a conversation turn fails because an agent could not be built or could not complete
 * execution (model error, tool failure, unimplemented department agent, etc.). Maps to HTTP 502 in
 * {@code GlobalExceptionHandler} - the client's request was well-formed, but the agent layer could
 * not fulfill it.
 */
public class AgentExecutionException extends ShrijaAiException {

  public AgentExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
