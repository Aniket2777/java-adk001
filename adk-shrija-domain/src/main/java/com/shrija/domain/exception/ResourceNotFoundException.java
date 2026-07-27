package com.shrija.domain.exception;

/**
 * Thrown when a lookup by identifier finds nothing. Generic on purpose: every department agent will
 * have this shape of failure (employee not found, payroll record not found, budget line not
 * found...) - one type mapped once in {@code GlobalExceptionHandler}, subclassed per domain for a
 * clear error message, rather than a new exception + handler pair per agent.
 */
public class ResourceNotFoundException extends ShrijaAiException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
