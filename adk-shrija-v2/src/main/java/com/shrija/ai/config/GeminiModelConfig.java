package com.shrija.ai.config;

import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the ADK {@link Gemini} model as a shared Spring bean so every agent factory (manager +
 * department agents) injects the same configured model instance instead of constructing its own.
 *
 * <p><b>Verify against your local {@code core} module:</b> the constructor signature for {@code
 * com.google.adk.models.Gemini} shown here ({@code modelName, apiKey}) is based on the package
 * layout you shared (models package under {@code com.google.adk}); confirm the exact
 * constructor/builder against the jar in {@code adk-java/core/src/main/java/com/google/adk/models}.
 */
@Configuration
public class GeminiModelConfig {

  @Bean
  public Gemini geminiModel(ShrijaAiProperties properties) {
    return new Gemini(properties.geminiModel(), properties.geminiApiKey());
  }
}
