package com.shrija.ai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Strongly-typed configuration for Shrija AI, bound from the {@code shrija.ai.*} prefix in
 * application.yml. Keeping this centralized avoids scattering {@code @Value} lookups across agent
 * factories and services, and gives us validation at startup instead of a NullPointer three layers
 * deep at runtime.
 */
@ConfigurationProperties(prefix = "shrija.ai")
@Validated
public record ShrijaAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    Jwt jwt,
    @NotBlank String mcpServerUrl) {
  public record Jwt(@NotBlank String secret, long expirationMillis) {}
}
