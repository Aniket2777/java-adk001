package com.shrija.ai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "shrija.ai")
@Validated
public record ShrijaAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    String employeeAgentUrl,
    Jwt jwt) {
  public record Jwt(@NotBlank String secret, long expirationMillis) {}
}
