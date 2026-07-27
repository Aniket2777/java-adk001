package com.shrija.domain.exception;

/**
 * Thrown when a request conflicts with existing state - a duplicate employee code/email, for
 * example. Distinct from BusinessRuleViolationException (400) because this maps to HTTP 409: the
 * request itself is fine, it's just already been done or collides with something that already
 * exists.
 */
public class ConflictException extends ShrijaAiException {

  public ConflictException(String message) {
    super(message);
  }
}
