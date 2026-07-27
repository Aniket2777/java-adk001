package com.shrija.domain.exception;

/**
 * Thrown when a request is well-formed but violates a domain rule (not enough leave balance, an
 * invalid date range, etc.) - distinct from {@link ResourceNotFoundException} because it maps to
 * HTTP 400, not 404. Generic on purpose, same reasoning as {@code ResourceNotFoundException}: one
 * type, one handler, subclassed per domain for a clear message.
 */
public class BusinessRuleViolationException extends ShrijaAiException {

  public BusinessRuleViolationException(String message) {
    super(message);
  }
}
