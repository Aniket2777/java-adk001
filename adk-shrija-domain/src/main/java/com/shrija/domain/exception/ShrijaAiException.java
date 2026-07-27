package com.shrija.domain.exception;

/**
 * Base type for all Shrija AI application exceptions. Keeping a single root lets {@code
 * GlobalExceptionHandler} catch application-specific failures distinctly from generic runtime
 * errors, and lets future subtypes (e.g. {@code AgentNotFoundException}, {@code
 * SessionExpiredException}) be added without touching the exception handler.
 */
public class ShrijaAiException extends RuntimeException {

  public ShrijaAiException(String message) {
    super(message);
  }

  public ShrijaAiException(String message, Throwable cause) {
    super(message, cause);
  }
}
