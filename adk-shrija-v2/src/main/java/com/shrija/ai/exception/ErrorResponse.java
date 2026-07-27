package com.shrija.ai.exception;

import java.time.Instant;

/**
 * Uniform error body returned by every failing endpoint, so API consumers can rely on one shape
 * regardless of which exception fired.
 */
public record ErrorResponse(
    Instant timestamp, int status, String error, String message, String path) {
  public static ErrorResponse of(int status, String error, String message, String path) {
    return new ErrorResponse(Instant.now(), status, error, message, path);
  }
}
